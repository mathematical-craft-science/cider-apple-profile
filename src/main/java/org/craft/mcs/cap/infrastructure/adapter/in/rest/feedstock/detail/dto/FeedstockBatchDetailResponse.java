package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.FeedstockBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementResponse;
import org.jspecify.annotations.NonNull;


/**
 * Transport read model for feedstock batch details.
 *
 * <p>The response is aggregate-oriented: batch metadata is primary, measurements are evidence and
 * current profile analysis is derived from the latest measurement.
 */
@Schema(description = "Feedstock batch detail read model.")
public record FeedstockBatchDetailResponse(
    @Schema(description = "Feedstock batch metadata.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FeedstockBatchResponse batch,
    @Schema(description = "Current profile and readiness state.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FeedstockBatchCurrentProfileResponse currentProfile,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Measurement history for the batch.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = FeedstockMeasurementResponse.class)
    )
    @NonNull List<@NonNull FeedstockMeasurementResponse> measurements
) {
}
