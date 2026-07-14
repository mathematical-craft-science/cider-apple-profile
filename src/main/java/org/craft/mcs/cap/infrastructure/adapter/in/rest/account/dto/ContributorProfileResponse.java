package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContactVisibility;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Complete management representation of the current account's contributor profile. */
@Schema(description = "CAP-owned contributor profile including private management fields and consent state.")
public record ContributorProfileResponse(
    @Schema(format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID userId,
    @Schema(implementation = ContributorType.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ContributorType contributorType,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String publicName,
    @Schema(format = "uri", nullable = true)
    @Nullable String website,
    @Schema(format = "email", nullable = true)
    @Nullable String publicEmail,
    @Schema(nullable = true)
    @Nullable String publicPhone,
    @Schema(implementation = ContactVisibility.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ContactVisibility contactVisibility,
    @Schema(format = "date-time", nullable = true)
    @Nullable Instant contactPublicationConsentedAt
) {
}
