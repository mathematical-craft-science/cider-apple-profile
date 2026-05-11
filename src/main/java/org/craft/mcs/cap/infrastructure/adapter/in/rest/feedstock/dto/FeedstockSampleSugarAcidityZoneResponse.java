package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileCapabilityAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityZoneAssessmentResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Feedstock sample sugar-acidity graph point and assessment response.")
public record FeedstockSampleSugarAcidityZoneResponse(
    @Schema(description = "Feedstock sample identifier.", format = "uuid", example = "ed7c0818-aad9-47dd-97c0-d1d37561ca18") @NonNull UUID sampleId,
    @Schema(description = "Cider maker identifier.", format = "uuid", example = "4d96759c-86ff-4d39-8b19-241bc60f82ff") @NonNull UUID ciderMakerId,
    @Schema(description = "Feedstock batch identifier.", format = "uuid", example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f") @NonNull UUID batchId,
    @Schema(description = "Sample name.", example = "Pressing sample #1") @NonNull String sampleName,
    @Schema(description = "Whether the sample has enough data to be plotted.", example = "true")
    boolean plottable,
    @Schema(description = "Zone assessment, when plottable.")
    @Nullable SugarAcidityZoneAssessmentResponse assessment,
    @Schema(description = "Capability assessment, present when the sample is not plottable.")
    @Nullable ChemicalProfileCapabilityAssessmentResponse capabilityAssessment
) {

  public FeedstockSampleSugarAcidityZoneResponse(
      @NonNull UUID sampleId,
      @NonNull UUID ciderMakerId,
      @NonNull UUID batchId,
      @NonNull String sampleName,
      @NonNull SugarAcidityZoneAssessmentResponse assessment
  ) {
    this(sampleId, ciderMakerId, batchId, sampleName, true, assessment, null);
  }
}
