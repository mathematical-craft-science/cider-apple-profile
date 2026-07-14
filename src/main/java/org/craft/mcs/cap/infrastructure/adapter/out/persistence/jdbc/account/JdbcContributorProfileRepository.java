package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.contributor.application.port.out.ContributorProfileRepository;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorProfile;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.AccountJdbcMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;

/** Persists the optional public contributor profile associated with a CAP account. */
@Repository
@RequiredArgsConstructor
class JdbcContributorProfileRepository implements ContributorProfileRepository {

  private final @NonNull SpringDataContributorProfileJdbcRepository repository;

  @Override
  public @NonNull ContributorProfile save(@NonNull ContributorProfile profile) {
    requireNonNull(profile, "contributor profile cannot be null");
    final var entity = repository.findById(profile.userId().value())
        .map(existing -> AccountJdbcMapper.updateFromDomain(profile, existing))
        .orElseGet(() -> AccountJdbcMapper.fromDomain(profile));
    final var saved = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "contributor profile",
        () -> repository.save(entity)
    );
    return AccountJdbcMapper.toDomain(saved);
  }

  @Override
  public void deleteByUserId(@NonNull AccountUserId userId) {
    requireNonNull(userId, "account user id cannot be null");
    repository.deleteById(userId.value());
  }

  @Override
  public @NonNull Optional<@NonNull ContributorProfile> findByUserId(@NonNull AccountUserId userId) {
    requireNonNull(userId, "account user id cannot be null");
    return repository.findById(userId.value()).map(AccountJdbcMapper::toDomain);
  }
}
