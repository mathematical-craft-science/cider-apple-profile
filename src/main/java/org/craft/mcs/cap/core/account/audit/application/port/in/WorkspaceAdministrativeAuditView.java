package org.craft.mcs.cap.core.account.audit.application.port.in;

import java.time.Instant;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeSubjectType;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeSubjectId;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAuditEntryId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Read model of an immutable workspace administration audit fact. */
public record WorkspaceAdministrativeAuditView(
    @NonNull WorkspaceAuditEntryId auditEntryId,
    @NonNull AccountUserId actorUserId,
    @NonNull WorkspaceAdministrativeAction action,
    @NonNull WorkspaceAdministrativeSubjectType subjectType,
    @NonNull WorkspaceAdministrativeSubjectId subjectId,
    @Nullable WorkspaceAdministrativeAuditValue previousValue,
    @Nullable WorkspaceAdministrativeAuditValue newValue,
    @NonNull Instant occurredAt
) {

  public WorkspaceAdministrativeAuditView {
    requireNonNull(auditEntryId, "workspace audit entry id cannot be null");
    requireNonNull(actorUserId, "audit actor user id cannot be null");
    requireNonNull(action, "workspace administrative action cannot be null");
    requireNonNull(subjectType, "workspace administrative subject type cannot be null");
    requireNonNull(subjectId, "workspace administrative subject id cannot be null");
    requireNonNull(occurredAt, "workspace administrative action time cannot be null");
  }
}
