package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static org.craft.mcs.cap.shared.infrastructure.rest.UuidV7Schema.FORMAT;
import static org.craft.mcs.cap.shared.infrastructure.rest.UuidV7Schema.PATTERN;


@Schema(description = "Request to create a feedstock batch.")
public record CreateFeedstockBatchRequest(
    @Schema(
        description = "Workspace identifier.",
        format = "uuid",
        example = "4d96759c-86ff-4d39-8b19-241bc60f82ff",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull @NonNull UUID workspaceId,
    @Schema(
        description = "Batch name.",
        example = "October Kingston Black harvest",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank @NonNull String name,
    @Schema(
        description = "UUIDv7 identifier of the operational vessel that holds the feedstock material.",
        format = FORMAT,
        pattern = PATTERN,
        example = "019fd42c-3ac8-7000-91fe-48173dfc0c7e",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull @NonNull UUID vesselId,
    @Schema(
        description = "Feedstock type.",
        implementation = FeedstockType.class,
        example = "JUICE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull @NonNull FeedstockType type,
    @Schema(
        description = "Harvest season date range.",
        implementation = FeedstockSeasonRequest.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Valid @NotNull @NonNull FeedstockSeasonRequest season,
    @Schema(
        description = "Optional feedstock source or origin.",
        example = "North orchard block A",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String source,
    @Schema(
        description = "Available feedstock volume in liters. Must be greater than zero.",
        example = "240.0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @DecimalMin(value = "0", inclusive = false)
    @NotNull @NonNull BigDecimal availableVolumeLiters,
    @Schema(
        description = "Optional batch note.",
        example = "Stored for two weeks before pressing.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String note
) {
}
