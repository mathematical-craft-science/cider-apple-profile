package org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneGeometryProfile;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.JolicoeurSugarAcidityGraphMetadataResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.JolicoeurSugarAcidityGraphMetadataRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** Provides the transport projection of the configured SG/TA zone geometry. */
@Component
@RequiredArgsConstructor
public final class JolicoeurSugarAcidityGraphMetadataResponseProvider {

  private final @NonNull JolicoeurSugarAcidityZoneGeometryProfile geometryProfile;

  @NonNull JolicoeurSugarAcidityGraphMetadataResponse get() {
    return JolicoeurSugarAcidityGraphMetadataRestMapper.toResponse(geometryProfile);
  }
}
