package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.audit;

import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.user.AccountUserJdbcEntity;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.workspace.WorkspaceJdbcEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import static java.util.Objects.requireNonNull;

/** Spring Data JDBC row for one immutable workspace administration audit fact. */
@Table("workspace_administrative_audit")
public class WorkspaceAuditJdbcEntity {

  @Id @NonNull @Column("audit_entry_id") UUID auditEntryId;
  @NonNull @Column("workspace_id") AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceId;
  @NonNull @Column("actor_user_id") AggregateReference<AccountUserJdbcEntity, @NonNull UUID> actorUserId;
  @NonNull @Column("action") WorkspaceAdministrativeAction action;
  @NonNull @Column("subject_type") WorkspaceAdministrativeSubjectType subjectType;
  @NonNull @Column("subject_id") UUID subjectId;
  @Nullable @Column("previous_value") String previousValue;
  @Nullable @Column("new_value") String newValue;
  @NonNull @Column("occurred_at") Instant occurredAt;
  @CreatedDate @Nullable @Column("created_at") Instant createdAt;
  @LastModifiedDate @Nullable @Column("updated_at") Instant updatedAt;
  @Version @Column("version") long version;

  @SuppressWarnings("NullAway.Init")
  public WorkspaceAuditJdbcEntity() {
  }

  WorkspaceAuditJdbcEntity(
      @NonNull UUID auditEntryId,
      @NonNull AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceId,
      @NonNull AggregateReference<AccountUserJdbcEntity, @NonNull UUID> actorUserId,
      @NonNull WorkspaceAdministrativeAction action,
      @NonNull WorkspaceAdministrativeSubjectType subjectType,
      @NonNull UUID subjectId,
      @Nullable String previousValue,
      @Nullable String newValue,
      @NonNull Instant occurredAt
  ) {
    this.auditEntryId = requireNonNull(auditEntryId, "workspace audit entry id cannot be null");
    this.workspaceId = requireNonNull(workspaceId, "workspace reference cannot be null");
    this.actorUserId = requireNonNull(actorUserId, "audit actor reference cannot be null");
    this.action = requireNonNull(action, "workspace administrative action cannot be null");
    this.subjectType = requireNonNull(subjectType, "workspace audit subject type cannot be null");
    this.subjectId = requireNonNull(subjectId, "workspace audit subject id cannot be null");
    this.previousValue = previousValue;
    this.newValue = newValue;
    this.occurredAt = requireNonNull(occurredAt, "workspace administrative action time cannot be null");
  }
}
