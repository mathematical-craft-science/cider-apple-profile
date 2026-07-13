package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.domain.model.value.AccountUserId;
import org.craft.mcs.cap.core.account.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.domain.model.value.IdentityProviderCode;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.AccountJdbcMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** Persists the AccountUser aggregate through Spring Data JDBC and restores its domain form. */
@Repository
@RequiredArgsConstructor
class JdbcAccountUserRepository implements AccountUserRepository {

  private final @NonNull SpringDataAccountUserJdbcRepository repository;

  @Override
  public @NonNull AccountUser save(@NonNull AccountUser user) {
    requireNonNull(user, "account user cannot be null");
    final var entity = repository.findById(user.userId().value())
        .map(existing -> AccountJdbcMapper.updateFromDomain(user, existing))
        .orElseGet(() -> AccountJdbcMapper.fromDomain(user));
    final var saved = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "account user",
        () -> repository.save(entity)
    );
    return AccountJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull Optional<@NonNull AccountUser> findById(@NonNull AccountUserId userId) {
    requireNonNull(userId, "account user id cannot be null");
    return repository.findById(userId.value()).map(AccountJdbcMapper::toDomain);
  }

  @Override
  public @NonNull Optional<@NonNull AccountUser> findByExternalIdentity(
      @NonNull IdentityProviderCode provider,
      @NonNull ExternalUserSubject subject
  ) {
    requireNonNull(provider, "identity provider cannot be null");
    requireNonNull(subject, "external user subject cannot be null");
    return repository.findByIdentityProviderAndExternalSubject(provider.value(), subject.value())
        .map(AccountJdbcMapper::toDomain);
  }
}
