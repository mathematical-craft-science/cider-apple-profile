package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.FeedstockBatchResponse;
import org.jspecify.annotations.NonNull;


/**
 * Transport read model for feedstock batch details.
 *
 * <p>The response is aggregate-oriented: batch metadata is primary and current profile analysis is
 * derived from the current material-state lineage. Measurement history is queried through the
 * generic Measurement API.
 */
@Schema(description = "Feedstock batch detail read model.")
public record FeedstockBatchDetailResponse(
    @Schema(description = "Feedstock batch metadata.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FeedstockBatchResponse batch,
    @Schema(
        description = "Current immutable state of the batch material.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID currentMaterialStateId,
    @Schema(description = "Current profile and readiness state.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FeedstockBatchCurrentProfileResponse currentProfile
) {
}
