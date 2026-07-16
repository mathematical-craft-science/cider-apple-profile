package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.audit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeSubjectType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Transport representation of one immutable workspace administration audit fact. */
@Schema(description = "Actor-aware workspace administration audit fact.")
public record WorkspaceAdministrativeAuditResponse(
    @Schema(format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID auditEntryId,
    @Schema(format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID actorUserId,
    @Schema(implementation = WorkspaceAdministrativeAction.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull WorkspaceAdministrativeAction action,
    @Schema(
        implementation = WorkspaceAdministrativeSubjectType.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull WorkspaceAdministrativeSubjectType subjectType,
    @Schema(format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID subjectId,
    @Schema(nullable = true)
    @Nullable String previousValue,
    @Schema(nullable = true)
    @Nullable String newValue,
    @Schema(format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull Instant occurredAt
) {
}
