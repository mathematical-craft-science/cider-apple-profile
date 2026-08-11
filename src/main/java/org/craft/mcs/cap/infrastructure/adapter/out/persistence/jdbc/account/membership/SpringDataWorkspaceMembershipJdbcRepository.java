package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.membership;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipStatus;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.user.AccountUserJdbcEntity;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.workspace.WorkspaceJdbcEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


/** Spring Data command repository for WorkspaceMembership JDBC entities. */
interface SpringDataWorkspaceMembershipJdbcRepository
    extends ListCrudRepository<WorkspaceMembershipJdbcEntity, @NonNull UUID> {

  @Modifying
  @Query("""
      UPDATE workspace_memberships
         SET role = :role,
             status = :status,
             version = version + 1,
             updated_at = GREATEST(clock_timestamp(), created_at, updated_at)
       WHERE membership_id = :membershipId
         AND version = :expectedVersion
      """)
  int updateByIdAndVersion(
      @Param("membershipId") @NonNull UUID membershipId,
      @Param("role") @NonNull AccountRole role,
      @Param("status") @NonNull WorkspaceMembershipStatus status,
      @Param("expectedVersion") long expectedVersion
  );

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
