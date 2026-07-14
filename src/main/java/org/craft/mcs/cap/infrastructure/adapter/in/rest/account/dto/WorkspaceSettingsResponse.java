package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.jspecify.annotations.NonNull;


/** Auditable workspace product settings with an optimistic-concurrency revision. */
@Schema(description = "Product settings shared by members of one CAP workspace.")
public record WorkspaceSettingsResponse(
    @Schema(
        description = "Workspace owning these settings.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID workspaceId,
    @Schema(
        description = "ISO-8601 duration after which measurement evidence is considered stale.",
        example = "PT336H",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String measurementStaleAfter,
    @Schema(
        description = "Default reference basis used to display titratable acidity.",
        implementation = TitratableAcidityBasis.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull TitratableAcidityBasis defaultAcidityBasis,
    @Schema(
        description = "Revision required as expectedRevision by the next update.",
        minimum = "0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    long revision,
    @Schema(
        description = "Time when workspace settings were created.",
        type = "string",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant createdAt,
    @Schema(
        description = "Time when workspace settings were last updated.",
        type = "string",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant updatedAt
) {
}
