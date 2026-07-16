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
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;


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

  @Modifying
  @Query("DELETE FROM workspace_memberships WHERE membership_id = :membershipId AND version = :version")
  int deleteByIdAndVersion(
      @Param("membershipId") @NonNull UUID membershipId,
      @Param("version") long version
  );
}
