package org.craft.mcs.cap.shared.domain.sugaracidity.assessment;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.domain.sugaracidity.plot.EllipseSugarAcidityGraphZone;
import org.craft.mcs.cap.shared.domain.sugaracidity.plot.SugarAcidityGraphMetadata;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class EllipseSugarAcidityZoneAssessor
    extends AbstractSugarAcidityZoneAssessor {

  private static final MathContext MC = MathContext.DECIMAL64;

  public EllipseSugarAcidityZoneAssessor(
      @NonNull SugarAcidityZoneThresholds thresholds,
      @NonNull SugarAcidityGraphMetadata graphMetadata
  ) {
    super(thresholds, graphMetadata);
  }

  @Override
  public @NonNull Optional<@NonNull SugarAcidityZoneAssessment> assess(
      @NonNull ChemicalProfileAxes axes
  ) {
    requireNonNull(axes, "chemical profile axes cannot be null");

    final var sugarValue = axisValue(axes, ChemicalAxis.SUGAR);
    final var acidityValue = axisValue(axes, ChemicalAxis.ACIDITY);

    if (sugarValue.isEmpty() || acidityValue.isEmpty()) {
      return Optional.empty();
    }

    final var point = profilePoint(axes);
    final var sugar = levelOf(
        sugarValue.orElseThrow(),
        thresholds().lowSugar(),
        thresholds().mediumSugar(),
        thresholds().highSugar()
    );
    final var acidity = levelOf(
        acidityValue.orElseThrow(),
        thresholds().lowAcidity(),
        thresholds().mediumAcidity(),
        thresholds().highAcidity()
    );
    final var tannin = tanninLevel(axes);

    return graphMetadata().zones()
        .stream()
        .filter(EllipseSugarAcidityGraphZone.class::isInstance)
        .map(EllipseSugarAcidityGraphZone.class::cast)
        .filter(zone -> contains(zone, sugarValue.orElseThrow(), acidityValue.orElseThrow()))
        .findFirst()
        .map(zone -> SugarAcidityZoneAssessment.classified(
            zone.zone(),
            sugar,
            acidity,
            tannin,
            zone.zone() == SugarAcidityZone.PERFECT_APPLES && isMedium(tannin),
            point
        ))
        .or(() -> Optional.of(SugarAcidityZoneAssessment.unclassified(
            sugar,
            acidity,
            tannin,
            point
        )));
  }

  private static boolean contains(
      @NonNull EllipseSugarAcidityGraphZone zone,
      @NonNull BigDecimal sugar,
      @NonNull BigDecimal acidity
  ) {
    final var normalizedSugar = sugar.subtract(zone.sugarCenter(), MC)
        .divide(zone.sugarRadius(), MC);
    final var normalizedAcidity = acidity.subtract(zone.acidityCenter(), MC)
        .divide(zone.acidityRadius(), MC);
    final var distance = normalizedSugar.multiply(normalizedSugar, MC)
        .add(normalizedAcidity.multiply(normalizedAcidity, MC), MC);

    return distance.compareTo(BigDecimal.ONE) <= 0;
  }
}
