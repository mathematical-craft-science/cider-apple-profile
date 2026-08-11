package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.user;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcVersionedUpdate.complete;

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
        () -> repository.save(AccountUserJdbcMapper.fromDomain(user))
    );
    return AccountUserJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull VersionedAggregate<AccountUser> update(
      @NonNull VersionedAggregate<AccountUser> user
  ) {
    requireNonNull(user, "versioned account user cannot be null");
    final var aggregate = user.aggregate();
    final var affectedRows = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "account user",
        () -> repository.updateByIdAndVersion(
            aggregate.userId().value(),
            aggregate.identityProvider().value(),
            aggregate.externalSubject().value(),
            aggregate.email().value(),
            aggregate.displayName().value(),
            aggregate.avatarUrl() == null ? null : aggregate.avatarUrl().value().toString(),
            aggregate.status(),
            user.revision().value()
        )
    );
    return complete("account user", user, affectedRows);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<AccountUser>> findVersionedById(
      @NonNull AccountUserId userId
  ) {
    requireNonNull(userId, "account user id cannot be null");
    return repository.findById(userId.value()).map(AccountUserJdbcMapper::toVersionedAggregate);
  }

  @Override
  public @NonNull List<@NonNull AccountUser> findAllByEmail(@NonNull EmailAddress email) {
    requireNonNull(email, "email address cannot be null");
    return repository.findAllByEmailOrderByUserId(email.value()).stream()
        .map(AccountUserJdbcMapper::toDomain)
        .toList();
  }

  @Override
  public @NonNull Optional<@NonNull AccountUser> findByExternalIdentity(
      @NonNull IdentityProviderCode provider,
      @NonNull ExternalUserSubject subject
  ) {
    requireNonNull(provider, "identity provider cannot be null");
    requireNonNull(subject, "external user subject cannot be null");
    return repository.findByIdentityProviderAndExternalSubject(provider.value(), subject.value())
        .map(AccountUserJdbcMapper::toDomain);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<AccountUser>> findVersionedByExternalIdentity(
      @NonNull IdentityProviderCode provider,
      @NonNull ExternalUserSubject subject
  ) {
    requireNonNull(provider, "identity provider cannot be null");
    requireNonNull(subject, "external user subject cannot be null");
    return repository.findByIdentityProviderAndExternalSubject(provider.value(), subject.value())
        .map(AccountUserJdbcMapper::toVersionedAggregate);
  }
}
