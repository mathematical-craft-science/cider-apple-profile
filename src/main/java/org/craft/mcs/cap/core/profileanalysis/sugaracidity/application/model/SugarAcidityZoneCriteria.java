package org.craft.mcs.cap.core.profileanalysis.sugaracidity.application.model;

import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record SugarAcidityZoneCriteria(
    @NonNull Set<@NonNull SugarAcidityZone> zones
) {

  public SugarAcidityZoneCriteria {
    requireNonNull(zones, "sugar-acidity zones cannot be null");
    zones = Set.copyOf(zones);

    if (zones.isEmpty()) {
      throw new ApplicationValidationException("sugar-acidity zones cannot be empty");
    }
  }

  public static @NonNull SugarAcidityZoneCriteria of(@NonNull SugarAcidityZone zone) {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
    return new SugarAcidityZoneCriteria(Set.of(zone));
  }
}
