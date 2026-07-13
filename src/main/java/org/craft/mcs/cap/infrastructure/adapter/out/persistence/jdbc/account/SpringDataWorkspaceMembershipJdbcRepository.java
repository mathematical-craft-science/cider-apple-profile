package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.AccountUserJdbcEntity;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.WorkspaceJdbcEntity;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.WorkspaceMembershipJdbcEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.ListCrudRepository;


/** Spring Data command repository for WorkspaceMembership JDBC entities. */
interface SpringDataWorkspaceMembershipJdbcRepository
    extends ListCrudRepository<WorkspaceMembershipJdbcEntity, @NonNull UUID> {

  @NonNull List<@NonNull WorkspaceMembershipJdbcEntity> findAllByUserIdOrderByCreatedAtAscMembershipIdAsc(
      @NonNull AggregateReference<AccountUserJdbcEntity, @NonNull UUID> userId
  );

  @NonNull List<@NonNull WorkspaceMembershipJdbcEntity> findAllByWorkspaceIdOrderByCreatedAtAscMembershipIdAsc(
      @NonNull AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceId
  );

  @NonNull Optional<@NonNull WorkspaceMembershipJdbcEntity> findByWorkspaceIdAndUserId(
      @NonNull AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceId,
      @NonNull AggregateReference<AccountUserJdbcEntity, @NonNull UUID> userId
  );
}
