package org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.point.domain.SugarAcidityAxisPoint;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.point.domain.SugarAcidityProfilePoint;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Calculates a chemical profile composition by mass balance over source weights.
 *
 * <p>This is the default calculator for juice/feedstock composition. It assumes that every source is
 * already normalized to the common axes used by the cider analysis domain:
 * sugar as specific gravity, acidity as titratable acidity, and tannin as tannin concentration.
 *
 * <p>Formulae:
 * <ul>
 *   <li>Sugar: specific gravity is converted to gravity points, composed by weighted average, then
 *       converted back to SG: {@code SG = 1 + weightedAverage((SG_i - 1) * 1000) / 1000}.</li>
 *   <li>Acidity: concentration mass balance, {@code sum(acidity_i * weight_i) / sum(weight_i)}.</li>
 *   <li>Tannin: same concentration mass balance when the selected tannin policy allows it.</li>
 * </ul>
 *
 * <p>The strategy does not decide which source weights to use. It is called by fixed composition
 * calculations and by optimization algorithms after they generate a candidate set of weights.
 *
 * Sugar is composed through gravity points: gp = (SG - 1.000) * 1000.
 * Acidity and tannin are composed as concentration mass balance:
 * blended = sum(value_i * weight_i) / sum(weight_i).
 */
public final class MassBalanceChemicalProfileCompositionStrategy
    implements ChemicalProfileCompositionCalculator {

  private static final BigDecimal ONE = BigDecimal.ONE;
  private static final BigDecimal GRAVITY_POINTS_FACTOR = new BigDecimal("1000");
  private static final int CALCULATION_SCALE = 9;

  @Override
  public @NonNull ChemicalProfileComposition calculate(@NonNull ChemicalProfileCompositionRequest request) {
    requireNonNull(request, "chemical profile composition request cannot be null");
    validateWeightMode(request.sources());
    validateAvailableVolume(request.sources());

    final var warnings = new ArrayList<ChemicalProfileCompositionWarning>();
    final var sugar = calculateSugar(request.sources());
    final var acidity = calculateAxis(request.sources(), ChemicalAxis.ACIDITY);
    final var tannin = calculateTannin(request, warnings);

    return ChemicalProfileComposition.of(
        request.sources(),
        new SugarAcidityProfilePoint(
            calculatedPoint(sugar),
            calculatedPoint(acidity),
            tannin == null ? null : calculatedPoint(tannin)
        ),
        warnings
    );
  }

  private static void validateWeightMode(@NonNull List<@NonNull ChemicalProfileCompositionSource> sources) {
    final var firstWeightClass = sources.getFirst().weight().getClass();
    final var mixedWeights = sources.stream()
        .map(source -> source.weight().getClass())
        .anyMatch(weightClass -> !weightClass.equals(firstWeightClass));

    if (mixedWeights) {
      throw new InvalidChemicalProfileCompositionException(
          "chemical profile composition cannot mix absolute volumes and proportions");
    }
  }

  private static void validateAvailableVolume(@NonNull List<@NonNull ChemicalProfileCompositionSource> sources) {
    for (final var source : sources) {
      if (!(source.weight() instanceof AbsoluteCompositionVolume(VolumeValue volume))
          || source.availableVolume() == null) {
        continue;
      }

      if (volume.compareTo(source.availableVolume()) > 0) { // NOSONAR
        throw new InvalidChemicalProfileCompositionException(
            "composition source requested volume exceeds available volume: " + sourceReference(source)
        );
      }
    }
  }

  private static @NonNull BigDecimal calculateSugar(
      @NonNull List<@NonNull ChemicalProfileCompositionSource> sources
  ) {
    final var blendedGravityPoints = weightedAverage(
        sources,
        source -> {
          final var sg = requiredAxis(source, ChemicalAxis.SUGAR).value();
          return sg.subtract(ONE).multiply(GRAVITY_POINTS_FACTOR);
        }
    );

    return ONE.add(blendedGravityPoints.divide(GRAVITY_POINTS_FACTOR, CALCULATION_SCALE, RoundingMode.HALF_UP));
  }

  private static @NonNull BigDecimal calculateAxis(
      @NonNull List<@NonNull ChemicalProfileCompositionSource> sources,
      @NonNull ChemicalAxis axis
  ) {
    return weightedAverage(
        sources,
        source -> requiredAxis(source, axis).value()
    );
  }

  private static @Nullable BigDecimal calculateTannin(
      @NonNull ChemicalProfileCompositionRequest request,
      @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings
  ) {
    return switch (request.tanninPolicy()) {
      case IGNORE_TANNIN -> {
        if (request.sources().stream().anyMatch(source -> source.axes().has(ChemicalAxis.TANNINITY))) {
          warnings.add(ChemicalProfileCompositionWarning.TANNIN_IGNORED);
        }
        yield null;
      }
      case REQUIRE_TANNIN -> calculateAxis(request.sources(), ChemicalAxis.TANNINITY);
      case CALCULATE_WHEN_COMPLETE -> calculateTanninWhenComplete(request.sources(), warnings);
    };
  }

  private static @Nullable BigDecimal calculateTanninWhenComplete(
      @NonNull List<@NonNull ChemicalProfileCompositionSource> sources,
      @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings
  ) {
    final var tanninCount = sources.stream()
        .filter(source -> source.axes().has(ChemicalAxis.TANNINITY))
        .count();

    if (tanninCount == sources.size()) {
      return calculateAxis(sources, ChemicalAxis.TANNINITY);
    }
    if (tanninCount > 0) {
      warnings.add(ChemicalProfileCompositionWarning.TANNIN_INCOMPLETE);
    }

    return null;
  }

  private static @NonNull AxisValue requiredAxis(
      @NonNull ChemicalProfileCompositionSource source,
      @NonNull ChemicalAxis axis
  ) {
    return source.axes().valueOf(axis)
        .orElseThrow(() -> new InvalidChemicalProfileCompositionException(
            "composition source is missing required axis %s: %s".formatted(axis, sourceReference(source))
        ));
  }

  private static @NonNull String sourceReference(@NonNull ChemicalProfileCompositionSource source) {
    final var reference = source.reference();
    return reference.type() + ":" + reference.id();
  }

  private static @NonNull BigDecimal weightedAverage(
      @NonNull List<@NonNull ChemicalProfileCompositionSource> sources,
      @NonNull SourceValueExtractor extractor
  ) {
    var weightedSum = BigDecimal.ZERO;
    var totalWeight = BigDecimal.ZERO;

    for (final var source : sources) {
      final var weight = source.weight().value();
      weightedSum = weightedSum.add(extractor.valueOf(source).multiply(weight));
      totalWeight = totalWeight.add(weight);
    }

    return weightedSum.divide(totalWeight, CALCULATION_SCALE, RoundingMode.HALF_UP);
  }

  private static @NonNull SugarAcidityAxisPoint calculatedPoint(@NonNull BigDecimal value) {
    return new SugarAcidityAxisPoint(
        value,
        MeasurementValueKind.CALCULATED,
        null,
        null,
        null,
        null
    );
  }

  @FunctionalInterface
  private interface SourceValueExtractor {

    @NonNull
    BigDecimal valueOf(@NonNull ChemicalProfileCompositionSource source);
  }
}
