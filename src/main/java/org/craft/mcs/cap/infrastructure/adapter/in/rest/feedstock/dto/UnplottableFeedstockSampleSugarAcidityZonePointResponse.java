package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileCapabilityAssessmentResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Feedstock sample that cannot be plotted on the sugar-acidity graph.")
public record UnplottableFeedstockSampleSugarAcidityZonePointResponse(
    @Schema(description = "Feedstock sample identifier.", format = "uuid", example = "ed7c0818-aad9-47dd-97c0-d1d37561ca18") @NonNull UUID sampleId,
    @Schema(description = "Cider maker identifier.", format = "uuid", example = "4d96759c-86ff-4d39-8b19-241bc60f82ff") @NonNull UUID ciderMakerId,
    @Schema(description = "Feedstock batch identifier.", format = "uuid", example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f") @NonNull UUID batchId,
    @Schema(description = "Sample name.", example = "Pressing sample #1") @NonNull String sampleName,
    @Schema(description = "Capability assessment explaining why the sample cannot be plotted.")
    @NonNull ChemicalProfileCapabilityAssessmentResponse capabilityAssessment
) {
}
