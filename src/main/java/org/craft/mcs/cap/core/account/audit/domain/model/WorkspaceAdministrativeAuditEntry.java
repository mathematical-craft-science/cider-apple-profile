package org.craft.mcs.cap.core.account.audit.domain.model;

import java.time.Instant;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeSubjectId;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAuditEntryId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jmolecules.ddd.annotation.Entity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Append-only fact identifying who performed a workspace administration change and when. */
@Entity
public record WorkspaceAdministrativeAuditEntry(
    @NonNull WorkspaceAuditEntryId auditEntryId,
    @NonNull WorkspaceId workspaceId,
    @NonNull AccountUserId actorUserId,
    @NonNull WorkspaceAdministrativeAction action,
    @NonNull WorkspaceAdministrativeSubjectType subjectType,
    @NonNull WorkspaceAdministrativeSubjectId subjectId,
    @Nullable WorkspaceAdministrativeAuditValue previousValue,
    @Nullable WorkspaceAdministrativeAuditValue newValue,
    @NonNull Instant occurredAt
) {

  public WorkspaceAdministrativeAuditEntry {
    requireNonNull(auditEntryId, "workspace audit entry id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(actorUserId, "audit actor user id cannot be null");
    requireNonNull(action, "workspace administrative action cannot be null");
    requireNonNull(subjectType, "workspace administrative subject type cannot be null");
    requireNonNull(subjectId, "workspace administrative subject id cannot be null");
    requireNonNull(occurredAt, "workspace administrative action time cannot be null");
  }

  public static @NonNull WorkspaceAdministrativeAuditEntry create(
      @NonNull WorkspaceId workspaceId,
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceAdministrativeAction action,
      @NonNull WorkspaceAdministrativeSubjectType subjectType,
      @NonNull WorkspaceAdministrativeSubjectId subjectId,
      @Nullable WorkspaceAdministrativeAuditValue previousValue,
      @Nullable WorkspaceAdministrativeAuditValue newValue,
      @NonNull Instant occurredAt
  ) {
    return new WorkspaceAdministrativeAuditEntry(
        WorkspaceAuditEntryId.newId(),
        workspaceId,
        actorUserId,
        action,
        subjectType,
        subjectId,
        previousValue,
        newValue,
        occurredAt
    );
  }
}
