package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.account.contributor;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.contributor.application.port.out.ContributorProfileRepository;
import org.craft.mcs.cap.core.account.contributor.domain.model.ContributorProfile;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcVersionedUpdate.complete;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcVersionedUpdate.requireSingleRow;

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
        () -> repository.save(ContributorProfileJdbcMapper.fromDomain(profile))
    );
    return ContributorProfileJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull VersionedAggregate<ContributorProfile> update(
      @NonNull VersionedAggregate<ContributorProfile> profile
  ) {
    requireNonNull(profile, "versioned contributor profile cannot be null");
    final var aggregate = profile.aggregate();
    final var affectedRows = JdbcPersistenceExceptionTranslator.translateOptimisticLocking(
        "contributor profile",
        () -> repository.updateByIdAndVersion(
            aggregate.userId().value(),
            aggregate.contributorType(),
            aggregate.publicName().value(),
            aggregate.website() == null ? null : aggregate.website().value().toString(),
            aggregate.publicEmail() == null ? null : aggregate.publicEmail().value(),
            aggregate.publicPhone() == null ? null : aggregate.publicPhone().value(),
            aggregate.contactVisibility(),
            aggregate.contactPublicationConsentedAt(),
            profile.revision().value()
        )
    );
    return complete("contributor profile", profile, affectedRows);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<ContributorProfile>> findVersionedById(
      @NonNull AccountUserId userId
  ) {
    requireNonNull(userId, "account user id cannot be null");
    return repository.findById(userId.value()).map(ContributorProfileJdbcMapper::toVersionedAggregate);
  }

  @Override
  public void delete(@NonNull VersionedAggregate<ContributorProfile> profile) {
    requireNonNull(profile, "versioned contributor profile cannot be null");
    final var affectedRows = repository.deleteByIdAndVersion(
        profile.aggregate().userId().value(),
        profile.revision().value()
    );
    requireSingleRow("contributor profile", affectedRows);
  }
}
