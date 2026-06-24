package org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain.SugarAcidityProfilePoint;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record SugarAcidityCoordinates(
    @NonNull BigDecimal sugar,
    @NonNull BigDecimal acidity
) {

  public SugarAcidityCoordinates {
    requireNonNull(sugar, "sugar coordinate cannot be null");
    requireNonNull(acidity, "acidity coordinate cannot be null");
  }

  public static @NonNull SugarAcidityCoordinates from(
      @NonNull SugarAcidityProfilePoint point
  ) {
    requireNonNull(point, "sugar-acidity profile point cannot be null");
    return new SugarAcidityCoordinates(point.sugar().value(), point.acidity().value());
  }
}
