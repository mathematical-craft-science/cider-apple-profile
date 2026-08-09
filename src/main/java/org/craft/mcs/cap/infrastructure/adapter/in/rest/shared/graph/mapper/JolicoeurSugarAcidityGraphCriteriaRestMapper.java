package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper;

import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphCriteria;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityZoneCriteria;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.jspecify.annotations.Nullable;


/** Maps Jolicoeur graph filter transport values to scenario-specific application criteria. */
public final class JolicoeurSugarAcidityGraphCriteriaRestMapper {

  private JolicoeurSugarAcidityGraphCriteriaRestMapper() {
  }

  public static @Nullable JolicoeurSugarAcidityZoneCriteria toZoneCriteria(
      @Nullable JolicoeurSugarAcidityZone zone
  ) {
    return zone == null ? null : JolicoeurSugarAcidityZoneCriteria.of(zone);
  }

  public static @Nullable JolicoeurSugarAcidityGraphCriteria toGraphCriteria(
      @Nullable JolicoeurSugarAcidityZone zone,
      @Nullable Boolean plottable
  ) {
    final var zoneCriteria = toZoneCriteria(zone);
    return zoneCriteria == null && plottable == null
        ? null
        : new JolicoeurSugarAcidityGraphCriteria(zoneCriteria, plottable);
  }
}
