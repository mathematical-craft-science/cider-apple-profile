package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockProfileEvidenceResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Current feedstock batch fermentation-start readiness response.")
public record FeedstockBatchCurrentFermentationStartReadinessResponse(
    @Schema(
        description = "Feedstock batch identifier.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID batchId,
    @Schema(description = "Workspace identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID workspaceId,
    @Schema(
        description = "Material state whose composite current profile was assessed.",
        format = "uuid",
        example = "019fb533-6d2e-7000-8f1c-b64f74d52079",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID currentMaterialStateId,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Per-characteristic SG, pH and YAN evidence used for readiness assessment.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = FeedstockProfileEvidenceResponse.class)
    )
    @NonNull List<@NonNull FeedstockProfileEvidenceResponse> profileEvidence,
    @Schema(description = "Readiness assessment.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FermentationStartReadinessAssessmentResponse assessment
) {
}
