package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.point.SugarAcidityProfilePoint;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record SugarAcidityZoneAssessment(
    boolean classified,
    @Nullable SugarAcidityZone zone,
    @NonNull SugarAcidityLevel sugar,
    @NonNull SugarAcidityLevel acidity,
    @Nullable SugarAcidityLevel tannin,
    boolean balancedForSingleVariety,
    @NonNull SugarAcidityProfilePoint point
) {

  public SugarAcidityZoneAssessment {
    if (classified && zone == null) {
      throw new IllegalArgumentException("classified assessment must have a zone");
    }
    requireNonNull(sugar, "sugar level cannot be null");
    requireNonNull(acidity, "acidity level cannot be null");
    requireNonNull(point, "sugar-acidity profile point cannot be null");
  }

  public static @NonNull SugarAcidityZoneAssessment classified(
      @NonNull SugarAcidityZone zone,
      @NonNull SugarAcidityLevel sugar,
      @NonNull SugarAcidityLevel acidity,
      @Nullable SugarAcidityLevel tannin,
      boolean balancedForSingleVariety,
      @NonNull SugarAcidityProfilePoint point
  ) {
    return new SugarAcidityZoneAssessment(
        true,
        requireNonNull(zone, "sugar-acidity zone cannot be null"),
        sugar,
        acidity,
        tannin,
        balancedForSingleVariety,
        point
    );
  }

  public static @NonNull SugarAcidityZoneAssessment unclassified(
      @NonNull SugarAcidityLevel sugar,
      @NonNull SugarAcidityLevel acidity,
      @Nullable SugarAcidityLevel tannin,
      @NonNull SugarAcidityProfilePoint point
  ) {
    return new SugarAcidityZoneAssessment(false, null, sugar, acidity, tannin, false, point);
  }
}
