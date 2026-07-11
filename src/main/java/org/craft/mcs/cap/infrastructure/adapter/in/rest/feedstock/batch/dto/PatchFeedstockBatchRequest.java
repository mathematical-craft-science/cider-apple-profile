package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Request to partially update editable feedstock batch fields.")
public record PatchFeedstockBatchRequest(
    @Schema(
        description = "Cider maker identifier used for ownership validation.",
        format = "uuid",
        example = "4d96759c-86ff-4d39-8b19-241bc60f82ff",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID ciderMakerId,
    @Schema(
        description = "New batch name, when changed.",
        example = "October Kingston Black harvest",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String name,
    @Schema(
        description = "New vessel or tank where the batch is stored, when changed. Blank value clears the vessel.",
        example = "Tank A-01",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String vessel,
    @Schema(
        description = "New feedstock type, when changed.",
        implementation = FeedstockType.class,
        example = "JUICE",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockType type,
    @Schema(
        description = "New season date range, when changed.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockSeasonRequest season,
    @Schema(
        description = "New source or origin. Blank value clears the source.",
        example = "North orchard block A",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String source,
    @Schema(
        description = "New available volume in liters, when changed. Must be greater than zero.",
        example = "240.0",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @DecimalMin(value = "0", inclusive = false)
    @Nullable BigDecimal availableVolumeLiters,
    @Schema(
        description = "New note. Blank value clears the note.",
        example = "Stored for two weeks before pressing.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String note
) {
}
