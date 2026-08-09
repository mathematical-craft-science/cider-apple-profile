package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.invitation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.workspace.WorkspaceJdbcEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


/**
 * Spring Data command repository for WorkspaceInvitation JDBC entities.
 */
interface SpringDataWorkspaceInvitationJdbcRepository
    extends ListCrudRepository<WorkspaceInvitationJdbcEntity, @NonNull UUID> {

  @Modifying
  @Query("""
      UPDATE workspace_invitations
         SET status = :status,
             accepted_by_user_id = :acceptedByUserId,
             expires_at = :expiresAt,
             resend_count = :resendCount,
             version = version + 1,
             updated_at = GREATEST(clock_timestamp(), created_at, updated_at)
       WHERE invitation_id = :invitationId
         AND version = :expectedVersion
      """)
  int updateByIdAndVersion(
      @Param("invitationId") @NonNull UUID invitationId,
      @Param("status") @NonNull WorkspaceInvitationStatus status,
      @Param("acceptedByUserId") @Nullable UUID acceptedByUserId,
      @Param("expiresAt") @NonNull Instant expiresAt,
      @Param("resendCount") int resendCount,
      @Param("expectedVersion") long expectedVersion
  );

  @NonNull Optional<@NonNull WorkspaceInvitationJdbcEntity> findByWorkspaceIdAndInvitedEmailAndStatus(
      @NonNull AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceId,
      @NonNull String invitedEmail,
      @NonNull WorkspaceInvitationStatus status
  );

  @NonNull List<@NonNull WorkspaceInvitationJdbcEntity> findAllByWorkspaceIdOrderByCreatedAtDescInvitationIdDesc(
      @NonNull AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceId
  );

  @NonNull List<@NonNull WorkspaceInvitationJdbcEntity> findAllByInvitedEmailOrderByCreatedAtDescInvitationIdDesc(
      @NonNull String invitedEmail
  );
}
