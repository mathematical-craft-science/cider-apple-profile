package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.invitation;

import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
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

/** Spring Data JDBC aggregate entity for a pre-membership workspace invitation. */
@Table("workspace_invitations")
public class WorkspaceInvitationJdbcEntity {

  @Id @NonNull @Column("invitation_id") UUID invitationId;
  @NonNull @Column("workspace_id") AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceId;
  @NonNull @Column("invited_email") String invitedEmail;
  @NonNull @Column("role") AccountRole role;
  @NonNull @Column("status") WorkspaceInvitationStatus status;
  @NonNull @Column("invited_by_user_id") AggregateReference<AccountUserJdbcEntity, @NonNull UUID> invitedByUserId;
  @Nullable @Column("accepted_by_user_id") AggregateReference<AccountUserJdbcEntity, @NonNull UUID> acceptedByUserId;
  @NonNull @Column("expires_at") Instant expiresAt;
  @Column("resend_count") int resendCount;
  @CreatedDate @Nullable @Column("created_at") Instant createdAt;
  @LastModifiedDate @Nullable @Column("updated_at") Instant updatedAt;
  @Version @Column("version") long version;

  @SuppressWarnings("NullAway.Init")
  public WorkspaceInvitationJdbcEntity() {
  }

  WorkspaceInvitationJdbcEntity(
      @NonNull UUID invitationId,
      @NonNull AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceId,
      @NonNull String invitedEmail,
      @NonNull AccountRole role,
      @NonNull WorkspaceInvitationStatus status,
      @NonNull AggregateReference<AccountUserJdbcEntity, @NonNull UUID> invitedByUserId,
      @Nullable AggregateReference<AccountUserJdbcEntity, @NonNull UUID> acceptedByUserId,
      @NonNull Instant expiresAt,
      int resendCount
  ) {
    this.invitationId = requireNonNull(invitationId, "workspace invitation id cannot be null");
    this.workspaceId = requireNonNull(workspaceId, "workspace reference cannot be null");
    this.invitedEmail = requireNonNull(invitedEmail, "invited email cannot be null");
    this.role = requireNonNull(role, "invited workspace role cannot be null");
    this.status = requireNonNull(status, "workspace invitation status cannot be null");
    this.invitedByUserId = requireNonNull(invitedByUserId, "inviting account reference cannot be null");
    this.acceptedByUserId = acceptedByUserId;
    this.expiresAt = requireNonNull(expiresAt, "workspace invitation expiration cannot be null");
    this.resendCount = resendCount;
  }
}
