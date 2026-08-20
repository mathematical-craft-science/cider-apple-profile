package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.actor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.actor.application.contract.ActorDisplayView;
import org.craft.mcs.cap.core.account.actor.application.contract.ActorLifecycleStatus;
import org.craft.mcs.cap.core.account.actor.application.port.out.ActorDirectoryEntryReader;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUserStatus;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.ActorType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** Reads minimal human actor display data without loading the AccountUser aggregate. */
@Repository
@RequiredArgsConstructor
public class JdbcAccountUserActorDirectoryEntryReader implements ActorDirectoryEntryReader {

  private static final @NonNull String FIND_BY_ID = """
      SELECT account_user.user_id,
             account_user.display_name,
             account_user.avatar_url,
             account_user.status
      FROM workspace_memberships membership
      JOIN account_users account_user ON account_user.user_id = membership.user_id
      WHERE membership.workspace_id = :workspaceId
        AND membership.user_id = :actorId
      """;
  private static final @NonNull RowMapper<@NonNull ActorDisplayView> ACCOUNT_USER_ROW_MAPPER =
      (resultSet, _) -> toView(resultSet);

  private final @NonNull NamedParameterJdbcOperations jdbc;

  @Override
  public @NonNull ActorType actorType() {
    return ActorType.ACCOUNT_USER;
  }

  @Override
  public @NonNull Optional<@NonNull ActorDisplayView> findById(
      @NonNull WorkspaceId workspaceId,
      @NonNull UUID actorId
  ) {
    requireNonNull(workspaceId, "actor workspace id cannot be null");
    requireNonNull(actorId, "actor id cannot be null");
    return jdbc.query(
            FIND_BY_ID,
            Map.of("workspaceId", workspaceId.value(), "actorId", actorId),
            ACCOUNT_USER_ROW_MAPPER
        )
        .stream()
        .findFirst();
  }

  private static @NonNull ActorDisplayView toView(@NonNull ResultSet resultSet) throws SQLException {
    final var status = AccountUserStatus.valueOf(resultSet.getString("status"));
    return new ActorDisplayView(
        ActorReference.accountUser(resultSet.getObject("user_id", UUID.class)),
        DisplayName.of(resultSet.getString("display_name")),
        HttpUrl.fromNullable(resultSet.getString("avatar_url")),
        toActorStatus(status)
    );
  }

  private static @NonNull ActorLifecycleStatus toActorStatus(@NonNull AccountUserStatus status) {
    return switch (status) {
      case ACTIVE -> ActorLifecycleStatus.ACTIVE;
      case DISABLED -> ActorLifecycleStatus.INACTIVE;
      case DELETED -> ActorLifecycleStatus.DELETED;
    };
  }
}
