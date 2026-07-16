package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.contributor.application.port.out.ContributorProfileRepository;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorProfile;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.entity.AccountJdbcMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcAggregateRevisionGuard.requireExpectedRevision;

/** Persists the optional public contributor profile associated with a CAP account. */
@Repository
@RequiredArgsConstructor
class JdbcContributorProfileRepository implements ContributorProfileRepository {

  private final @NonNull SpringDataContributorProfileJdbcRepository repository;

  @Override
  public @NonNull ContributorProfile create(@NonNull ContributorProfile profile) {
    requireNonNull(profile, "contributor profile cannot be null");
    final var saved = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "contributor profile",
        () -> repository.save(AccountJdbcMapper.fromDomain(profile))
    );
    return AccountJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull VersionedAggregate<ContributorProfile> update(
      @NonNull VersionedAggregate<ContributorProfile> profile
  ) {
    requireNonNull(profile, "versioned contributor profile cannot be null");
    final var aggregate = profile.aggregate();
    final var entity = repository.findById(aggregate.userId().value())
        .orElseThrow(() -> new ConcurrentResourceModificationException("contributor profile", null));
    requireExpectedRevision("contributor profile", profile.revision(), AccountJdbcMapper.revision(entity));
    final var saved = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "contributor profile",
        () -> repository.save(AccountJdbcMapper.updateFromDomain(aggregate, entity))
    );
    return AccountJdbcMapper.toVersionedAggregate(saved);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<ContributorProfile>> findVersionedById(
      @NonNull AccountUserId userId
  ) {
    requireNonNull(userId, "account user id cannot be null");
    return repository.findById(userId.value()).map(AccountJdbcMapper::toVersionedAggregate);
  }

  @Override
  public void delete(@NonNull VersionedAggregate<ContributorProfile> profile) {
    requireNonNull(profile, "versioned contributor profile cannot be null");
    final var affectedRows = repository.deleteByIdAndVersion(
        profile.aggregate().userId().value(),
        profile.revision().value()
    );
    if (affectedRows != 1) {
      throw new ConcurrentResourceModificationException("contributor profile", null);
    }
  }
}
