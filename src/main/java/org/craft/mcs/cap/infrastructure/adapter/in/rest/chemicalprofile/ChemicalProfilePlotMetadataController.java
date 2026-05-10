package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityGraphMetadataResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.SugarAcidityGraphMetadataRestMapper;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.assessment.SugarAcidityZoneAssessor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class ChemicalProfilePlotMetadataController implements ChemicalProfilePlotMetadataApi {

  private final @NonNull SugarAcidityZoneAssessor sugarAcidityZoneAssessor;

  @Override
  @GetMapping(value = "/chemical-profiles/sugar-acidity-zone/plot-metadata", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull SugarAcidityGraphMetadataResponse> sugarAcidityPlotMetadata() {
    return ResponseEntity.ok(
        SugarAcidityGraphMetadataRestMapper.toResponse(
            sugarAcidityZoneAssessor.graphMetadata()
        )
    );
  }
}
