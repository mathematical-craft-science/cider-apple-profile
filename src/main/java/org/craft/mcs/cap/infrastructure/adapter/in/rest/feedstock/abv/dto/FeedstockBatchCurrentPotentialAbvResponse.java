package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.abv.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.jspecify.annotations.NonNull;


@Schema(description = "Potential ABV prediction response for a feedstock batch current profile.")
public record FeedstockBatchCurrentPotentialAbvResponse(
    @Schema(
        description = "Feedstock batch identifier.",
        format = "uuid",
        example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID batchId,
    @Schema(
        description = "Workspace identifier.",
        format = "uuid",
        example = "4d96759c-86ff-4d39-8b19-241bc60f82ff",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID workspaceId,
    @Schema(
        description = "Material state whose composite current profile was assessed.",
        format = "uuid",
        example = "019fb533-6d2e-7000-8f1c-b64f74d52079",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID currentMaterialStateId,
    @Schema(
        description = "Latest measurement set contributing to the composite current profile.",
        format = "uuid",
        example = "ed7c0818-aad9-47dd-97c0-d1d37561ca18",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID currentMeasurementId,
    @Schema(
        description = "Name of the latest measurement set contributing to the composite current profile.",
        example = "Pressing measurement #1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String currentMeasurementName,
    @Schema(
        description = "Source value kind used for original gravity.",
        implementation = AxisValueKind.class,
        example = "POINT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AxisValueKind originalGravityValueKind,
    @Schema(description = "Potential ABV prediction.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull PotentialAbvPredictionResponse prediction
) {
}
