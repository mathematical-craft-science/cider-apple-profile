package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper;

import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityZoneCriteria;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisCapability;
import org.craft.mcs.cap.core.profileanalysis.capability.application.model.ProfileAnalysisCapabilityCriteria;
import org.jspecify.annotations.Nullable;


/** Maps shared graph filter transport values to profile-analysis criteria. */
public final class ProfileGraphCriteriaRestMapper {

  private ProfileGraphCriteriaRestMapper() {
  }

  public static @Nullable JolicoeurSugarAcidityZoneCriteria toZoneCriteria(
      @Nullable JolicoeurSugarAcidityZone zone
  ) {
    return zone == null ? null : JolicoeurSugarAcidityZoneCriteria.of(zone);
  }

  public static @Nullable ProfileAnalysisCapabilityCriteria toCapabilityCriteria(
      @Nullable ProfileAnalysisCapability capability,
      @Nullable Boolean capable
  ) {
    return capability == null ? null : new ProfileAnalysisCapabilityCriteria(capability, capable);
  }
}
