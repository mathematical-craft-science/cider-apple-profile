package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.membership;

import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipStatus;
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

/** Spring Data JDBC aggregate entity linking a CAP user to a workspace. */
@Table("workspace_memberships")
public class WorkspaceMembershipJdbcEntity {

  @Id @NonNull @Column("membership_id") UUID membershipId;
  @NonNull @Column("workspace_id") AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceId;
  @NonNull @Column("user_id") AggregateReference<AccountUserJdbcEntity, @NonNull UUID> userId;
  @NonNull @Column("role") AccountRole role;
  @NonNull @Column("status") WorkspaceMembershipStatus status;
  @CreatedDate @Nullable @Column("created_at") Instant createdAt;
  @LastModifiedDate @Nullable @Column("updated_at") Instant updatedAt;
  @Version @Column("version") long version;

  @SuppressWarnings("NullAway.Init")
  public WorkspaceMembershipJdbcEntity() {
  }

  WorkspaceMembershipJdbcEntity(
      @NonNull UUID membershipId,
      @NonNull AggregateReference<WorkspaceJdbcEntity, @NonNull UUID> workspaceId,
      @NonNull AggregateReference<AccountUserJdbcEntity, @NonNull UUID> userId,
      @NonNull AccountRole role,
      @NonNull WorkspaceMembershipStatus status
  ) {
    this.membershipId = requireNonNull(membershipId, "workspace membership id cannot be null");
    this.workspaceId = requireNonNull(workspaceId, "workspace reference cannot be null");
    this.userId = requireNonNull(userId, "account user reference cannot be null");
    this.role = requireNonNull(role, "workspace role cannot be null");
    this.status = requireNonNull(status, "workspace membership status cannot be null");
  }
}
