package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.point;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record SugarAcidityProfilePoint(
    @NonNull SugarAcidityAxisPoint sugar,
    @NonNull SugarAcidityAxisPoint acidity,
    @Nullable SugarAcidityAxisPoint tannin
) {

  public SugarAcidityProfilePoint {
    requireNonNull(sugar, "sugar point cannot be null");
    requireNonNull(acidity, "acidity point cannot be null");
  }
}
