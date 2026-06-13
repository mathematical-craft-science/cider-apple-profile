package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileCapabilityAssessmentResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Feedstock measurement that cannot be plotted on the sugar-acidity graph.")
public record UnplottableFeedstockMeasurementSugarAcidityZonePointResponse(
    @Schema(description = "Feedstock measurement identifier.", format = "uuid", example = "ed7c0818-aad9-47dd-97c0-d1d37561ca18", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull UUID measurementId,
    @Schema(description = "Cider maker identifier.", format = "uuid", example = "4d96759c-86ff-4d39-8b19-241bc60f82ff", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull UUID ciderMakerId,
    @Schema(description = "Feedstock batch identifier.", format = "uuid", example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull UUID batchId,
    @Schema(description = "Measurement name.", example = "Pressing measurement #1", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull String measurementName,
    @Schema(description = "Capability assessment explaining why the measurement cannot be plotted.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ChemicalProfileCapabilityAssessmentResponse capabilityAssessment
) {
}
