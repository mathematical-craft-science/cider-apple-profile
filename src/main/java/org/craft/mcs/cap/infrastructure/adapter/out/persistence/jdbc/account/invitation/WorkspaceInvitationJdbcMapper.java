package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.invitation;

import java.util.UUID;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.user.AccountUserJdbcEntity;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.workspace.WorkspaceJdbcEntity;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

/** Maps the WorkspaceInvitation aggregate and its typed aggregate references. */
final class WorkspaceInvitationJdbcMapper {

  private WorkspaceInvitationJdbcMapper() {
  }

  static @NonNull WorkspaceInvitationJdbcEntity fromDomain(
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

  static @NonNull WorkspaceInvitation toDomain(@NonNull WorkspaceInvitationJdbcEntity entity) {
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

  static @NonNull VersionedAggregate<WorkspaceInvitation> toVersionedAggregate(
      @NonNull WorkspaceInvitationJdbcEntity entity
  ) {
    return new VersionedAggregate<>(toDomain(entity), AggregateRevision.of(entity.version));
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
}
