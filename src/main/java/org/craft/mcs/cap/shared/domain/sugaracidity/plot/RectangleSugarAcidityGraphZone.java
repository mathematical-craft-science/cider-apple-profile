package org.craft.mcs.cap.shared.domain.sugaracidity.plot;

import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityZone;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record RectangleSugarAcidityGraphZone(
    @NonNull SugarAcidityZone zone,
    @NonNull SugarAcidityGraphRange sugarRange,
    @NonNull SugarAcidityGraphRange acidityRange
) implements SugarAcidityGraphZone {

  public RectangleSugarAcidityGraphZone {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
    requireNonNull(sugarRange, "zone sugar range cannot be null");
    requireNonNull(acidityRange, "zone acidity range cannot be null");
  }

  @Override
  public @NonNull SugarAcidityGraphZoneShape shape() {
    return SugarAcidityGraphZoneShape.RECTANGLE;
  }
}
