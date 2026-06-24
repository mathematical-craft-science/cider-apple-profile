package org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.graph.domain.SugarAcidityGraphMetadata;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityGraphMetadataResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper.SugarAcidityGraphMetadataRestMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class SugarAcidityPlotMetadataController implements SugarAcidityPlotMetadataApi {

  private final @NonNull SugarAcidityGraphMetadata sugarAcidityGraphMetadata;

  @Override
  @GetMapping(value = "/chemical-profiles/sugar-acidity-zone/plot-metadata", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull SugarAcidityGraphMetadataResponse> sugarAcidityPlotMetadata() {
    return ResponseEntity.ok(
        SugarAcidityGraphMetadataRestMapper.toResponse(
            sugarAcidityGraphMetadata
        )
    );
  }
}
