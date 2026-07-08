package org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record SugarAcidityZoneGeometryProfile(
    @NonNull List<@NonNull SugarAcidityZoneGeometry> zones
) {

  public SugarAcidityZoneGeometryProfile {
    zones = List.copyOf(requireNonNull(zones, "sugar-acidity zone geometries cannot be null"));
  }

  public @NonNull Optional<@NonNull SugarAcidityZoneGeometry> find(
      @NonNull SugarAcidityZone zone
  ) {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
    return zones.stream()
        .filter(geometry -> geometry.zone() == zone)
        .findFirst();
  }
}
