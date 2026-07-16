package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.administration;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.shared.application.port.out.WorkspaceAdministrationLock;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;

/** PostgreSQL transaction lock serializing cross-membership workspace administration invariants. */
@Repository
@RequiredArgsConstructor
class PostgresWorkspaceAdministrationLock implements WorkspaceAdministrationLock {

  private static final @NonNull String ACQUIRE_LOCK_SQL = """
      SELECT pg_advisory_xact_lock(hashtextextended(:workspaceId, 0))
      """;

  private final @NonNull JdbcClient jdbcClient;

  @Override
  public void acquire(@NonNull WorkspaceId workspaceId) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    jdbcClient.sql(ACQUIRE_LOCK_SQL)
        .param("workspaceId", workspaceId.value().toString())
        .query((_, _) -> Boolean.TRUE)
        .single();
  }
}
