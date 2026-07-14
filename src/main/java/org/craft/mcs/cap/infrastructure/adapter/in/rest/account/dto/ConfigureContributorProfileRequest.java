package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Full replacement request for the current account's contributor profile. */
@Schema(description = "Contributor identity and explicit contact publication choice.")
public record ConfigureContributorProfileRequest(
    @Schema(implementation = ContributorType.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull @NonNull ContributorType contributorType,
    @Schema(example = "North Orchard Laboratory", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank @NonNull String publicName,
    @Schema(format = "uri", nullable = true)
    @Nullable String website,
    @Schema(format = "email", nullable = true)
    @Email @Nullable String publicEmail,
    @Schema(nullable = true)
    @Nullable String publicPhone,
    @Schema(
        description = "Explicit consent to publish the supplied email and phone.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull @NonNull Boolean publishContacts
) {
}
