package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Partial workspace-settings update based on the revision last observed by the client. */
@Schema(description = "Partial update of CAP workspace product settings.")
public record UpdateWorkspaceSettingsRequest(
    @Schema(
        description = "Revision returned by the latest settings read.",
        minimum = "0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Long expectedRevision,
    @Schema(
        description = "Optional ISO-8601 measurement freshness duration.",
        example = "PT504H",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String measurementStaleAfter,
    @Schema(
        description = "Optional default titratable-acidity reference basis.",
        implementation = TitratableAcidityBasis.class,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable TitratableAcidityBasis defaultAcidityBasis
) {

  public UpdateWorkspaceSettingsRequest {
    requireNonNull(expectedRevision, "expected workspace settings revision cannot be null");
  }
}
