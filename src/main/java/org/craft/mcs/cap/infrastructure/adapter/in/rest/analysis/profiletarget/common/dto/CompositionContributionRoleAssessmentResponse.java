package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileEvaluationAxis;
import org.jspecify.annotations.NonNull;


@Schema(description = "Target-aware role of a profile source in a possible composition.")
public record CompositionContributionRoleAssessmentResponse(
    @ArraySchema(
        arraySchema = @Schema(
            description = "Roles assigned to the source in priority order; the first role is primary.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = CompositionContributionRole.class)
    )
    @NonNull List<@NonNull CompositionContributionRole> roles,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Per-axis explanation for assigned roles.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = CompositionContributionEvidenceResponse.class)
    )
    @NonNull List<@NonNull CompositionContributionEvidenceResponse> evidence,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Axes missing for role assessment.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = ProfileEvaluationAxis.class)
    )
    @NonNull List<@NonNull ProfileEvaluationAxis> missingAxes
) {
}
