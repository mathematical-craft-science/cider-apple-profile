package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.target;

import org.craft.mcs.cap.core.cideranalysis.graph.domain.zone.JolicoeurSugarAcidityZoneId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Built-in Jolicoeur sugar-acidity zone resolved to concrete target geometry for scoring. */
public record PredefinedJolicoeurSugarAcidityZoneTarget(
    @NonNull JolicoeurSugarAcidityZoneId zone,
    @NonNull SugarAcidityTargetGeometry geometry
) implements SugarAcidityTarget {

  public PredefinedJolicoeurSugarAcidityZoneTarget {
    requireNonNull(zone, "sugar-acidity zone cannot be null");
    requireNonNull(geometry, "sugar-acidity target geometry cannot be null");
  }

  @Override
  public @NonNull SugarAcidityTargetKind kind() {
    return SugarAcidityTargetKind.PREDEFINED_ZONE;
  }
}
