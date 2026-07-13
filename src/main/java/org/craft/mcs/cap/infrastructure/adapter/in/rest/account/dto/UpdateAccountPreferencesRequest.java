package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Request for replacing the current user's CAP locale and time-zone preferences. */
@Schema(description = "CAP-owned locale and IANA time-zone preferences.")
public record UpdateAccountPreferencesRequest(
    @Schema(
        description = "Preferred locale represented as a BCP 47 language tag.",
        example = "en",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String locale,
    @Schema(
        description = "Preferred time zone represented as an IANA time-zone identifier.",
        example = "Europe/Moscow",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String timeZone
) {

  public UpdateAccountPreferencesRequest {
    requireNonNull(locale, "user locale cannot be null");
    requireNonNull(timeZone, "user time zone cannot be null");
  }
}
