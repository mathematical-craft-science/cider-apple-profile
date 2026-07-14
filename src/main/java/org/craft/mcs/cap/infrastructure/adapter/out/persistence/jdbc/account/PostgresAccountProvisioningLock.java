package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.provisioning.application.port.out.AccountProvisioningLock;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;

/** PostgreSQL transaction lock preventing duplicate first-login account provisioning. */
@Repository
@RequiredArgsConstructor
class PostgresAccountProvisioningLock implements AccountProvisioningLock {

  private static final @NonNull String ACQUIRE_LOCK_SQL = """
      SELECT pg_advisory_xact_lock(hashtextextended(:externalIdentity, 0))
      """;

  private final @NonNull JdbcClient jdbcClient;

  @Override
  public void acquire(
      @NonNull IdentityProviderCode provider,
      @NonNull ExternalUserSubject subject
  ) {
    requireNonNull(provider, "identity provider cannot be null");
    requireNonNull(subject, "external user subject cannot be null");
    final var externalIdentity = provider.value() + ':' + subject.value();

    jdbcClient.sql(ACQUIRE_LOCK_SQL)
        .param("externalIdentity", externalIdentity)
        .query((_, _) -> Boolean.TRUE)
        .single();
  }
}
