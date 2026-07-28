package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Calculates composition characteristics by mass balance over source weights.
 *
 * <p>Specific Gravity (SG) is composed through gravity points. Titratable Acidity (TA), tannin,
 * and YAN are composed as concentration mass balances. Source intervals are interpreted only
 * through the interval policy carried by the request.
 */
public final class MassBalanceChemicalProfileCompositionStrategy
    implements ChemicalProfileCompositionCalculator {

  private static final BigDecimal ONE = BigDecimal.ONE;
  private static final BigDecimal GRAVITY_POINTS_FACTOR = new BigDecimal("1000");
  private static final int CALCULATION_SCALE = 9;

  @Override
  public @NonNull ChemicalProfileComposition calculate(@NonNull ChemicalProfileCompositionRequest request) {
    requireNonNull(request, "chemical profile composition request cannot be null");

    final var warnings = new ArrayList<ChemicalProfileCompositionWarning>();
    final var tannin = calculateTannin(request, warnings);
    final var yan = calculateYeastAssimilableNitrogen(request, warnings);
    final var characteristics = new CalculatedCompositionCharacteristics(
        SpecificGravityValue.of(calculateSpecificGravity(request)),
        TitratableAcidityValue.of(calculateTitratableAcidity(request)),
        tannin == null ? null : TanninValue.of(tannin),
        yan == null ? null : YeastAssimilableNitrogenValue.of(yan)
    );

    return ChemicalProfileComposition.of(request.sources(), characteristics, warnings);
  }

  private static @NonNull BigDecimal calculateSpecificGravity(
      @NonNull ChemicalProfileCompositionRequest request
  ) {
    final var blendedGravityPoints = weightedAverage(
        request.sources(),
        source -> representativeSpecificGravity(request, source)
            .subtract(ONE)
            .multiply(GRAVITY_POINTS_FACTOR)
    );

    return ONE.add(
        blendedGravityPoints.divide(GRAVITY_POINTS_FACTOR, CALCULATION_SCALE, RoundingMode.HALF_UP)
    );
  }

  private static @NonNull BigDecimal representativeSpecificGravity(
      @NonNull ChemicalProfileCompositionRequest request,
      @NonNull ChemicalProfileCompositionSource source
  ) {
    return request.intervalPolicy().representativeValue(
        source.characteristics().specificGravity()
    );
  }

  private static @NonNull BigDecimal calculateTitratableAcidity(
      @NonNull ChemicalProfileCompositionRequest request
  ) {
    return weightedAverage(
        request.sources(),
        source -> request.intervalPolicy().representativeValue(
            source.characteristics().titratableAcidity()
        )
    );
  }

  private static @Nullable BigDecimal calculateTannin(
      @NonNull ChemicalProfileCompositionRequest request,
      @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings
  ) {
    return switch (request.tanninPolicy()) {
      case IGNORE_TANNIN -> {
        if (request.sources().stream().anyMatch(MassBalanceChemicalProfileCompositionStrategy::hasTannin)) {
          warnings.add(ChemicalProfileCompositionWarning.TANNIN_IGNORED);
        }
        yield null;
      }
      case REQUIRE_TANNIN -> calculateRequiredTannin(request);
      case CALCULATE_WHEN_COMPLETE -> calculateTanninWhenComplete(request, warnings);
    };
  }

  private static boolean hasTannin(@NonNull ChemicalProfileCompositionSource source) {
    return source.characteristics().tannin() != null;
  }

  private static @NonNull BigDecimal calculateRequiredTannin(
      @NonNull ChemicalProfileCompositionRequest request
  ) {
    return weightedAverage(
        request.sources(),
        source -> request.intervalPolicy().representativeValue(
            requireTannin(source)
        )
    );
  }

  private static @Nullable BigDecimal calculateTanninWhenComplete(
      @NonNull ChemicalProfileCompositionRequest request,
      @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings
  ) {
    final var tanninCount = request.sources().stream()
        .filter(MassBalanceChemicalProfileCompositionStrategy::hasTannin)
        .count();

    if (tanninCount == request.sources().size()) {
      return calculateRequiredTannin(request);
    }
    if (tanninCount > 0) {
      warnings.add(ChemicalProfileCompositionWarning.TANNIN_INCOMPLETE);
    }
    return null;
  }

  private static @Nullable BigDecimal calculateYeastAssimilableNitrogen(
      @NonNull ChemicalProfileCompositionRequest request,
      @NonNull List<@NonNull ChemicalProfileCompositionWarning> warnings
  ) {
    final var yanCount = request.sources().stream()
        .filter(MassBalanceChemicalProfileCompositionStrategy::hasYeastAssimilableNitrogen)
        .count();

    if (yanCount == request.sources().size()) {
      return weightedAverage(
          request.sources(),
          source -> request.intervalPolicy().representativeValue(
              requireYeastAssimilableNitrogen(source)
          )
      );
    }
    if (yanCount > 0) {
      warnings.add(ChemicalProfileCompositionWarning.YEAST_ASSIMILABLE_NITROGEN_INCOMPLETE);
    }
    return null;
  }

  private static boolean hasYeastAssimilableNitrogen(
      @NonNull ChemicalProfileCompositionSource source
  ) {
    return source.characteristics().yeastAssimilableNitrogen() != null;
  }

  private static @NonNull CharacteristicValue<TanninValue> requireTannin(
      @NonNull ChemicalProfileCompositionSource source
  ) {
    final var tannin = source.characteristics().tannin();
    if (tannin == null) {
      throw missingCharacteristic(source, "tannin");
    }
    return tannin;
  }

  private static @NonNull CharacteristicValue<YeastAssimilableNitrogenValue>
      requireYeastAssimilableNitrogen(@NonNull ChemicalProfileCompositionSource source) {
    final var yan = source.characteristics().yeastAssimilableNitrogen();
    if (yan == null) {
      throw missingCharacteristic(source, "YAN");
    }
    return yan;
  }

  private static @NonNull InvalidChemicalProfileCompositionException missingCharacteristic(
      @NonNull ChemicalProfileCompositionSource source,
      @NonNull String characteristic
  ) {
    final var reference = source.reference();
    return new InvalidChemicalProfileCompositionException(
        "composition source is missing required %s: %s:%s"
            .formatted(characteristic, reference.type(), reference.id())
    );
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

  @FunctionalInterface
  private interface SourceValueExtractor {

    @NonNull
    BigDecimal valueOf(@NonNull ChemicalProfileCompositionSource source);
  }
}
