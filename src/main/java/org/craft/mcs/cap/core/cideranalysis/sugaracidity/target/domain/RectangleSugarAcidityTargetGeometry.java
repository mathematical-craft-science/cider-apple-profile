package org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.RectangleSugarAcidityZoneGeometry;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record RectangleSugarAcidityTargetGeometry(
    @NonNull BigDecimal sugarMin,
    @NonNull BigDecimal sugarMax,
    @NonNull BigDecimal acidityMin,
    @NonNull BigDecimal acidityMax
) implements SugarAcidityTargetGeometry {

  public RectangleSugarAcidityTargetGeometry {
    requireNonNull(sugarMin, "target sugar minimum cannot be null");
    requireNonNull(sugarMax, "target sugar maximum cannot be null");
    requireNonNull(acidityMin, "target acidity minimum cannot be null");
    requireNonNull(acidityMax, "target acidity maximum cannot be null");
    if (sugarMin.compareTo(sugarMax) > 0) {
      throw new DomainRuleViolationException("target sugar minimum cannot be greater than maximum");
    }
    if (acidityMin.compareTo(acidityMax) > 0) {
      throw new DomainRuleViolationException("target acidity minimum cannot be greater than maximum");
    }
  }

  public static @NonNull RectangleSugarAcidityTargetGeometry from(
      @NonNull RectangleSugarAcidityZoneGeometry zone
  ) {
    requireNonNull(zone, "rectangle sugar-acidity zone geometry cannot be null");
    return new RectangleSugarAcidityTargetGeometry(
        zone.sugarRange().min(),
        zone.sugarRange().max(),
        zone.acidityRange().min(),
        zone.acidityRange().max()
    );
  }

  @Override
  public @NonNull SugarAcidityTargetGeometryShape shape() {
    return SugarAcidityTargetGeometryShape.RECTANGLE;
  }

  @Override
  public boolean contains(@NonNull SugarAcidityCoordinates point) {
    requireNonNull(point, "sugar-acidity point cannot be null");
    return point.sugar().compareTo(sugarMin) >= 0
        && point.sugar().compareTo(sugarMax) <= 0
        && point.acidity().compareTo(acidityMin) >= 0
        && point.acidity().compareTo(acidityMax) <= 0;
  }

  @Override
  public @NonNull SugarAcidityCoordinates nearestPointTo(
      @NonNull SugarAcidityCoordinates point
  ) {
    requireNonNull(point, "sugar-acidity point cannot be null");
    return new SugarAcidityCoordinates(
        clamp(point.sugar(), sugarMin, sugarMax),
        clamp(point.acidity(), acidityMin, acidityMax)
    );
  }

  private static @NonNull BigDecimal clamp(
      @NonNull BigDecimal value,
      @NonNull BigDecimal min,
      @NonNull BigDecimal max
  ) {
    if (value.compareTo(min) < 0) {
      return min;
    }
    if (value.compareTo(max) > 0) {
      return max;
    }
    return value;
  }
}
