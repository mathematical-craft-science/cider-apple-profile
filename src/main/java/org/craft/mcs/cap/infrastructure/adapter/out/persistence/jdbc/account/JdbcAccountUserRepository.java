package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.AccountJdbcMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcAggregateRevisionGuard.requireExpectedRevision;

/** Persists the AccountUser aggregate through Spring Data JDBC and restores its domain form. */
@Repository
@RequiredArgsConstructor
class JdbcAccountUserRepository implements AccountUserRepository {

  private final @NonNull SpringDataAccountUserJdbcRepository repository;

  @Override
  public @NonNull AccountUser create(@NonNull AccountUser user) {
    requireNonNull(user, "account user cannot be null");
    final var saved = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "account user",
        () -> repository.save(AccountJdbcMapper.fromDomain(user))
    );
    return AccountJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull VersionedAggregate<AccountUser> update(
      @NonNull VersionedAggregate<AccountUser> user
  ) {
    requireNonNull(user, "versioned account user cannot be null");
    final var aggregate = user.aggregate();
    final var entity = repository.findById(aggregate.userId().value())
        .orElseThrow(() -> new ConcurrentResourceModificationException("account user", null));
    requireExpectedRevision("account user", user.revision(), AccountJdbcMapper.revision(entity));
    final var saved = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "account user",
        () -> repository.save(AccountJdbcMapper.updateFromDomain(aggregate, entity))
    );
    return AccountJdbcMapper.toVersionedAggregate(saved);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<AccountUser>> findVersionedById(
      @NonNull AccountUserId userId
  ) {
    requireNonNull(userId, "account user id cannot be null");
    return repository.findById(userId.value()).map(AccountJdbcMapper::toVersionedAggregate);
  }

  @Override
  public @NonNull Optional<@NonNull AccountUser> findByEmail(@NonNull EmailAddress email) {
    requireNonNull(email, "email address cannot be null");
    return repository.findByEmailIgnoreCase(email.value()).map(AccountJdbcMapper::toDomain);
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

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<AccountUser>> findVersionedByExternalIdentity(
      @NonNull IdentityProviderCode provider,
      @NonNull ExternalUserSubject subject
  ) {
    requireNonNull(provider, "identity provider cannot be null");
    requireNonNull(subject, "external user subject cannot be null");
    return repository.findByIdentityProviderAndExternalSubject(provider.value(), subject.value())
        .map(AccountJdbcMapper::toVersionedAggregate);
  }
}
