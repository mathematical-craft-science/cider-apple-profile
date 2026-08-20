package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.context;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.context.application.contract.WorkspaceMembershipSnapshot;
import org.craft.mcs.cap.core.account.context.application.port.out.CurrentAccountWorkspaceQuery;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipStatus;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceStatus;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** JDBC read adapter resolving active memberships and workspaces without aggregate N+1 loading. */
@Repository
@RequiredArgsConstructor
class JdbcCurrentAccountWorkspaceQuery implements CurrentAccountWorkspaceQuery {

  private static final @NonNull String FIND_ACTIVE_BY_USER_ID_SQL = """
      SELECT membership.membership_id,
             membership.workspace_id,
             membership.role,
             membership.status AS membership_status,
             workspace.name AS workspace_name,
             workspace.type AS workspace_type,
             workspace.status AS workspace_status
      FROM workspace_memberships membership
      JOIN workspaces workspace ON workspace.workspace_id = membership.workspace_id
      WHERE membership.user_id = :userId
        AND membership.status = 'ACTIVE'
        AND workspace.status = 'ACTIVE'
      """;

  private final @NonNull JdbcClient jdbcClient;

  @Override
  public @NonNull List<@NonNull WorkspaceMembershipSnapshot> findActiveByUserId(
      @NonNull AccountUserId userId
  ) {
    return jdbcClient.sql(FIND_ACTIVE_BY_USER_ID_SQL)
        .param("userId", userId.value())
        .query(JdbcCurrentAccountWorkspaceQuery::mapSnapshot)
        .list();
  }

  private static @NonNull WorkspaceMembershipSnapshot mapSnapshot(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    return new WorkspaceMembershipSnapshot(
        WorkspaceMembershipId.of(resultSet.getObject("membership_id", UUID.class)),
        WorkspaceId.of(resultSet.getObject("workspace_id", UUID.class)),
        DisplayName.of(resultSet.getString("workspace_name")),
        WorkspaceType.valueOf(resultSet.getString("workspace_type")),
        WorkspaceStatus.valueOf(resultSet.getString("workspace_status")),
        AccountRole.valueOf(resultSet.getString("role")),
        WorkspaceMembershipStatus.valueOf(resultSet.getString("membership_status"))
    );
  }
}
