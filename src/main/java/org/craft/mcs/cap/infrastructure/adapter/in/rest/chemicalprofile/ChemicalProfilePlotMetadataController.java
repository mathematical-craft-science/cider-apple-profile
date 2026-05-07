package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityGraphMetadataResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.SugarAcidityGraphMetadataRestMapper;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityZoneAssessor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public final class ChemicalProfilePlotMetadataController {

  private final @NonNull SugarAcidityZoneAssessor sugarAcidityZoneAssessor;

  @GetMapping("/chemical-profiles/sugar-acidity-zone/plot-metadata")
  public @NonNull ResponseEntity<@NonNull SugarAcidityGraphMetadataResponse> sugarAcidityPlotMetadata() {
    return ResponseEntity.ok(
        SugarAcidityGraphMetadataRestMapper.toResponse(
            sugarAcidityZoneAssessor.graphMetadata()
        )
    );
  }
}
