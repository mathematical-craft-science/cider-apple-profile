package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import org.jspecify.annotations.NonNull;


@Schema(description = "Feedstock season date range response.")
public record FeedstockSeasonResponse(
    @Schema(description = "Season start date.", format = "date", example = "2026-09-15", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull LocalDate startsOn,
    @Schema(description = "Season end date.", format = "date", example = "2026-12-01", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull LocalDate endsOn
) {
}
