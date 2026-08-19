package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto.VesselReferenceResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Feedstock batch response.")
public record FeedstockBatchResponse(
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
        description = "Batch name.",
        example = "October Kingston Black harvest",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String name,
    @Schema(
        description = "Current vessel reference resolved from the workspace vessel catalog.",
        implementation = VesselReferenceResponse.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull VesselReferenceResponse vessel,
    @Schema(
        description = "Feedstock type.",
        implementation = FeedstockType.class,
        example = "JUICE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockType type,
    @Schema(
        description = "Harvest season date range.",
        implementation = FeedstockSeasonResponse.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockSeasonResponse season,
    @Schema(
        description = "Optional feedstock source or origin.",
        example = "North orchard block A",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String source,
    @Schema(
        description = "Material availability lifecycle of the feedstock batch.",
        implementation = BatchLifecycle.class,
        example = "ACTIVE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull BatchLifecycle lifecycle,
    @Schema(description = "Available feedstock volume.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse availableVolume,
    @Schema(
        description = "Optional batch note.",
        example = "Stored for two weeks before pressing.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String note
) {
}
