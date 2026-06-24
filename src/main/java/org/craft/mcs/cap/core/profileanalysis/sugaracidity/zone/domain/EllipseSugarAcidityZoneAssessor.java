package org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain;

import java.util.Optional;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Classifies profile points by checking whether they fall inside configured ellipse zone geometries.
 */
public final class EllipseSugarAcidityZoneAssessor
    extends AbstractSugarAcidityZoneAssessor {

  public EllipseSugarAcidityZoneAssessor(
      @NonNull SugarAcidityZoneThresholds thresholds,
      @NonNull SugarAcidityZoneGeometryProfile geometryProfile
  ) {
    super(thresholds, geometryProfile);
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

    return geometryProfile().zones()
        .stream()
        .filter(EllipseSugarAcidityZoneGeometry.class::isInstance)
        .map(EllipseSugarAcidityZoneGeometry.class::cast)
        .filter(zone -> zone.contains(sugarValue.orElseThrow(), acidityValue.orElseThrow()))
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
}
