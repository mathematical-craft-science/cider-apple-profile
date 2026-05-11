package org.craft.mcs.cap.shared.platform.query;

import java.util.Set;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.assessment.SugarAcidityZone;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record SugarAcidityZoneCriteria(
    @NonNull Set<@NonNull SugarAcidityZone> zones
) {

  public SugarAcidityZoneCriteria {
    requireNonNull(zones, "sugar-acidity zones cannot be null");
    zones = Set.copyOf(zones);

    if (zones.isEmpty()) {
      throw new IllegalArgumentException("sugar-acidity zones cannot be empty");
    }
  }

  public static @NonNull SugarAcidityZoneCriteria of(@NonNull SugarAcidityZone zone) {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
    return new SugarAcidityZoneCriteria(Set.of(zone));
  }
}
