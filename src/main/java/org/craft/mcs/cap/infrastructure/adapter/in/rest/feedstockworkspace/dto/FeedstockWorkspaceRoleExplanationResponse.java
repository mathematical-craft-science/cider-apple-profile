package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;


@Schema(description = "Semantic facts extending target-aware contribution evidence for frontend interpretation.")
public record FeedstockWorkspaceRoleExplanationResponse(
    @ArraySchema(
        arraySchema = @Schema(
            description = "Typed semantic facts aligned by axis with contribution-role evidence.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = FeedstockWorkspaceRoleEvidenceFactResponse.class)
    )
    @NonNull List<@NonNull FeedstockWorkspaceRoleEvidenceFactResponse> evidence
) {
}
