package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZone;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record PredefinedSugarAcidityZoneTarget(
    @NonNull SugarAcidityZone zone,
    @NonNull SugarAcidityTargetGeometry geometry
) implements SugarAcidityTarget {

  public PredefinedSugarAcidityZoneTarget {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
    requireNonNull(geometry, "sugar-acidity target geometry cannot be null");
  }

  @Override
  public @NonNull SugarAcidityTargetKind kind() {
    return SugarAcidityTargetKind.PREDEFINED_ZONE;
  }
}
