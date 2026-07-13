package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NonNull;


/** Provider-independent presentation preferences owned by CAP. */
@Schema(description = "CAP-owned locale and time-zone preferences.")
public record AccountPreferencesResponse(
    @Schema(
        description = "User locale represented as a BCP 47 language tag.",
        example = "en",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String locale,
    @Schema(
        description = "User time zone represented as an IANA time-zone identifier.",
        example = "Europe/Moscow",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String timeZone
) {
}
