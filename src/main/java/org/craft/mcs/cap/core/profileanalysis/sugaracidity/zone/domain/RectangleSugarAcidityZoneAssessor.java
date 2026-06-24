package org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain;

import java.util.Optional;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Applies Jolicoeur rectangular sugar/acidity threshold rules to classify profile points.
 */
public final class RectangleSugarAcidityZoneAssessor
    extends AbstractSugarAcidityZoneAssessor {

  public RectangleSugarAcidityZoneAssessor(
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

    if (is(sugar, SugarAcidityLevel.HIGH) && is(acidity, SugarAcidityLevel.MEDIUM)) {
      return Optional.of(SugarAcidityZoneAssessment.classified(
          SugarAcidityZone.PERFECT_APPLES,
          sugar,
          acidity,
          tannin,
          isMedium(tannin),
          point
      ));
    }

    if (is(sugar, SugarAcidityLevel.HIGH) && is(acidity, SugarAcidityLevel.HIGH)) {
      return Optional.of(SugarAcidityZoneAssessment.classified(
          SugarAcidityZone.SUGAR_RICH_APPLES,
          sugar,
          acidity,
          tannin,
          false,
          point
      ));
    }

    if (is(acidity, SugarAcidityLevel.LOW)) {
      return Optional.of(SugarAcidityZoneAssessment.classified(
          SugarAcidityZone.LOW_ACIDITY_APPLES,
          sugar,
          acidity,
          tannin,
          false,
          point
      ));
    }

    if (is(sugar, SugarAcidityLevel.MEDIUM)
        && (is(acidity, SugarAcidityLevel.MEDIUM) || is(acidity, SugarAcidityLevel.HIGH))) {
      return Optional.of(SugarAcidityZoneAssessment.classified(
          SugarAcidityZone.MEDIUM_SUGAR_APPLES,
          sugar,
          acidity,
          tannin,
          false,
          point
      ));
    }

    if (is(sugar, SugarAcidityLevel.LOW)
        && (is(acidity, SugarAcidityLevel.MEDIUM) || is(acidity, SugarAcidityLevel.HIGH))) {
      return Optional.of(SugarAcidityZoneAssessment.classified(
          SugarAcidityZone.LOW_SUGAR_APPLES,
          sugar,
          acidity,
          tannin,
          false,
          point
      ));
    }

    return Optional.of(SugarAcidityZoneAssessment.unclassified(
        sugar,
        acidity,
        tannin,
        point
    ));
  }
}
