package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.membership;

import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

/**
 * Maps the WorkspaceMembership aggregate and its typed aggregate references.
 */
final class WorkspaceMembershipJdbcMapper {

  private WorkspaceMembershipJdbcMapper() {
  }

  static @NonNull WorkspaceMembershipJdbcEntity fromDomain(
      @NonNull WorkspaceMembership membership
  ) {
    return new WorkspaceMembershipJdbcEntity(
        membership.membershipId().value(),
        AggregateReference.to(membership.workspaceId().value()),
        AggregateReference.to(membership.userId().value()),
        membership.role(),
        membership.status()
    );
  }

  static @NonNull WorkspaceMembership toDomain(@NonNull WorkspaceMembershipJdbcEntity entity) {
    return new WorkspaceMembership(
        WorkspaceMembershipId.of(entity.membershipId),
        WorkspaceId.of(entity.workspaceId.getId()),
        AccountUserId.of(entity.userId.getId()),
        entity.role,
        entity.status
    );
  }

  static @NonNull VersionedAggregate<WorkspaceMembership> toVersionedAggregate(
      @NonNull WorkspaceMembershipJdbcEntity entity
  ) {
    return new VersionedAggregate<>(toDomain(entity), AggregateRevision.of(entity.version));
  }

}
