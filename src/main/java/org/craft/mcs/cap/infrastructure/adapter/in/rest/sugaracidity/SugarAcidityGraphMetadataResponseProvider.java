package org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneGeometryProfile;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphMetadataResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.SugarAcidityGraphMetadataRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** Provides the transport projection of the configured SG/TA zone geometry. */
@Component
@RequiredArgsConstructor
public final class SugarAcidityGraphMetadataResponseProvider {

  private final @NonNull JolicoeurSugarAcidityZoneGeometryProfile geometryProfile;

  @NonNull SugarAcidityGraphMetadataResponse get() {
    return SugarAcidityGraphMetadataRestMapper.toResponse(geometryProfile);
  }
}
