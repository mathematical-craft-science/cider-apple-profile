package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.audit.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;


/**
 * Workspace administration audit collection ordered from newest to oldest.
 */
@Schema(description = "Immutable workspace administration history.")
public record WorkspaceAdministrativeAuditListResponse(
    @ArraySchema(
        schema = @Schema(implementation = WorkspaceAdministrativeAuditResponse.class),
        arraySchema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    )
    @NonNull List<@NonNull WorkspaceAdministrativeAuditResponse> entries
) {
}
