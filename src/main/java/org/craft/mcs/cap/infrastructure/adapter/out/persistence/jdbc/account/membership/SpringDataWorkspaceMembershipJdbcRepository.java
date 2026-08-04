package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.membership;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipStatus;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.user.AccountUserJdbcEntity;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.workspace.WorkspaceJdbcEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.ListCrudRepository;


/** Spring Data command repository for WorkspaceMembership JDBC entities. */
interface SpringDataWorkspaceMembershipJdbcRepository
    extends ListCrudRepository<WorkspaceMembershipJdbcEntity, @NonNull UUID> {

  @NonNull List<@NonNull WorkspaceMembershipJdbcEntity>
      findAllByWorkspaceIdAndStatusNotOrderByCreatedAtAscMembershipIdAsc(
      @NonNull AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceId,
      @NonNull WorkspaceMembershipStatus status
  );

  @NonNull Optional<@NonNull WorkspaceMembershipJdbcEntity> findByWorkspaceIdAndUserId(
      @NonNull AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceId,
      @NonNull AggregateReference<AccountUserJdbcEntity, @NonNull UUID> userId
  );
}
