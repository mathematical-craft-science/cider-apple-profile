package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity;

import java.util.UUID;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAuditEntry;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeSubjectId;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAuditEntryId;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

/** Maps workspace administration aggregates and append-only audit facts to JDBC records. */
public final class WorkspaceAdministrationJdbcMapper {

  private WorkspaceAdministrationJdbcMapper() {
  }

  public static @NonNull WorkspaceInvitationJdbcEntity fromDomain(
      @NonNull WorkspaceInvitation invitation
  ) {
    return new WorkspaceInvitationJdbcEntity(
        invitation.invitationId().value(),
        workspaceReference(invitation.workspaceId()),
        invitation.invitedEmail().value(),
        invitation.role(),
        invitation.status(),
        userReference(invitation.invitedByUserId()),
        invitation.acceptedByUserId() == null ? null : userReference(invitation.acceptedByUserId()),
        invitation.expiresAt(),
        invitation.resendCount()
    );
  }

  public static @NonNull WorkspaceInvitationJdbcEntity updateFromDomain(
      @NonNull WorkspaceInvitation invitation,
      @NonNull WorkspaceInvitationJdbcEntity entity
  ) {
    entity.workspaceId = workspaceReference(invitation.workspaceId());
    entity.invitedEmail = invitation.invitedEmail().value();
    entity.role = invitation.role();
    entity.status = invitation.status();
    entity.invitedByUserId = userReference(invitation.invitedByUserId());
    entity.acceptedByUserId = invitation.acceptedByUserId() == null
        ? null
        : userReference(invitation.acceptedByUserId());
    entity.expiresAt = invitation.expiresAt();
    entity.resendCount = invitation.resendCount();
    return entity;
  }

  public static @NonNull WorkspaceInvitation toDomain(
      @NonNull WorkspaceInvitationJdbcEntity entity
  ) {
    return new WorkspaceInvitation(
        WorkspaceInvitationId.of(entity.invitationId),
        WorkspaceId.of(entity.workspaceId.getId()),
        EmailAddress.of(entity.invitedEmail),
        entity.role,
        entity.status,
        AccountUserId.of(entity.invitedByUserId.getId()),
        entity.acceptedByUserId == null ? null : AccountUserId.of(entity.acceptedByUserId.getId()),
        entity.expiresAt,
        entity.resendCount
    );
  }

  public static @NonNull WorkspaceAuditJdbcEntity fromDomain(
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

  public static @NonNull WorkspaceAdministrativeAuditEntry toDomain(
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
