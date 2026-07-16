package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.audit;

import java.util.UUID;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAuditEntry;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeSubjectId;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAuditEntryId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.user.AccountUserJdbcEntity;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.workspace.WorkspaceJdbcEntity;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

/** Maps append-only workspace administrative audit facts to JDBC rows. */
final class WorkspaceAuditJdbcMapper {

  private WorkspaceAuditJdbcMapper() {
  }

  static @NonNull WorkspaceAuditJdbcEntity fromDomain(
      @NonNull WorkspaceAdministrativeAuditEntry entry
  ) {
    return new WorkspaceAuditJdbcEntity(
        entry.auditEntryId().value(),
        workspaceReference(entry.workspaceId()),
        userReference(entry.actorUserId()),
        entry.action(),
        entry.subjectType(),
        entry.subjectId().value(),
        persistenceValue(entry.previousValue()),
        persistenceValue(entry.newValue()),
        entry.occurredAt()
    );
  }

  static @NonNull WorkspaceAdministrativeAuditEntry toDomain(
      @NonNull WorkspaceAuditJdbcEntity entity
  ) {
    return new WorkspaceAdministrativeAuditEntry(
        WorkspaceAuditEntryId.of(entity.auditEntryId),
        WorkspaceId.of(entity.workspaceId.getId()),
        AccountUserId.of(entity.actorUserId.getId()),
        entity.action,
        entity.subjectType,
        WorkspaceAdministrativeSubjectId.of(entity.subjectId),
        domainValue(entity.previousValue),
        domainValue(entity.newValue),
        entity.occurredAt
    );
  }

  private static @NonNull AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceReference(
      @NonNull WorkspaceId workspaceId
  ) {
    return AggregateReference.to(workspaceId.value());
  }

  private static @NonNull AggregateReference<AccountUserJdbcEntity, @NonNull UUID> userReference(
      @NonNull AccountUserId userId
  ) {
    return AggregateReference.to(userId.value());
  }

  private static @Nullable String persistenceValue(
      @Nullable WorkspaceAdministrativeAuditValue value
  ) {
    return value == null ? null : value.value();
  }

  private static @Nullable WorkspaceAdministrativeAuditValue domainValue(@Nullable String value) {
    return value == null ? null : WorkspaceAdministrativeAuditValue.of(value);
  }
}
