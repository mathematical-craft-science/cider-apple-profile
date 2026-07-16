package org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.graph.domain.SugarAcidityGraphMetadata;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityGraphMetadataResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper.SugarAcidityGraphMetadataRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** Provides the transport projection of configured sugar-acidity graph metadata. */
@Component
@RequiredArgsConstructor
public final class SugarAcidityGraphMetadataResponseProvider {

  private final @NonNull SugarAcidityGraphMetadata metadata;

  @NonNull SugarAcidityGraphMetadataResponse get() {
    return SugarAcidityGraphMetadataRestMapper.toResponse(metadata);
  }
}
