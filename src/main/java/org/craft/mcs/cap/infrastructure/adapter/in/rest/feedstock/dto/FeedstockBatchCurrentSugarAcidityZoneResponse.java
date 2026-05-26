package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileCapabilityAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.SugarAcidityZoneAssessmentResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Current feedstock batch sugar-acidity graph point and assessment response.")
public record FeedstockBatchCurrentSugarAcidityZoneResponse(
    @Schema(description = "Feedstock batch identifier.", format = "uuid", example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull UUID batchId,
    @Schema(description = "Cider maker identifier.", format = "uuid", example = "4d96759c-86ff-4d39-8b19-241bc60f82ff", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull UUID ciderMakerId,
    @Schema(description = "Current feedstock measurement identifier.", format = "uuid", example = "ed7c0818-aad9-47dd-97c0-d1d37561ca18", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull UUID currentMeasurementId,
    @Schema(description = "Current measurement name.", example = "Pressing measurement #1", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull String currentMeasurementName,
    @Schema(description = "Whether the current batch profile has enough data to be plotted.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean plottable,
    @Schema(description = "Zone assessment, when plottable.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable SugarAcidityZoneAssessmentResponse assessment,
    @Schema(description = "Capability assessment, present when the current batch profile is not plottable.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ChemicalProfileCapabilityAssessmentResponse capabilityAssessment
) {
}
