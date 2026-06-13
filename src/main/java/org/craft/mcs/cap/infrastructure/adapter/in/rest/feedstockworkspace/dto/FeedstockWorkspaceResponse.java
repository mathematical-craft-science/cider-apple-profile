package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Feedstock Workspace read model.")
public record FeedstockWorkspaceResponse(
    @ArraySchema(
        arraySchema = @Schema(description = "Feedstock Workspace batches.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = FeedstockWorkspaceBatchResponse.class)
    )
    @NonNull List<@NonNull FeedstockWorkspaceBatchResponse> batches,
    @Schema(description = "Target-analysis metadata when request contains target.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable FeedstockWorkspaceTargetAnalysisResponse targetAnalysis
) {
}
