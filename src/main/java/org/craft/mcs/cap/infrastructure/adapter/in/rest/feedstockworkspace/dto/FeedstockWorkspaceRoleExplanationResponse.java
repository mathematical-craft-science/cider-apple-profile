package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;


@Schema(description = "Compact explanation for the target-aware role of a feedstock batch.")
public record FeedstockWorkspaceRoleExplanationResponse(
    @Schema(description = "Short role summary.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String summary,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Short reasons behind the role assignment.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = String.class)
    )
    @NonNull List<@NonNull String> reasons
) {
}
