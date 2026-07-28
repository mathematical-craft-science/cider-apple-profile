package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile.entity.AppleVarietyJdbcMapper;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator.translateOptimisticLocking;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcAggregateRevisionGuard.requireExpectedRevision;


@Repository
@RequiredArgsConstructor
class JdbcAppleVarietyRepository implements AppleVarietyRepository {

  private final SpringDataAppleVarietyJdbcRepository repository;

  @Override
  @NonNull
  public AppleVariety create(@NonNull AppleVariety appleVariety) {
    requireNonNull(appleVariety, "apple variety cannot be null");

    final var saved = translateOptimisticLocking(
        "apple variety",
        () -> repository.save(AppleVarietyJdbcMapper.fromDomain(appleVariety))
    );

    return AppleVarietyJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull VersionedAggregate<AppleVariety> update(
      @NonNull VersionedAggregate<AppleVariety> appleVariety
  ) {
    requireNonNull(appleVariety, "versioned apple variety cannot be null");
    final var aggregate = appleVariety.aggregate();
    final var entity = repository.findById(aggregate.varietyId().value())
        .orElseThrow(() -> new ConcurrentResourceModificationException("apple variety", null));
    requireExpectedRevision("apple variety", appleVariety.revision(), AppleVarietyJdbcMapper.revision(entity));
    final var saved = translateOptimisticLocking(
        "apple variety",
        () -> repository.save(AppleVarietyJdbcMapper.updateFromDomain(aggregate, entity))
    );
    return AppleVarietyJdbcMapper.toVersionedAggregate(saved);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<AppleVariety>> findVersionedById(
      @NonNull AppleVarietyId varietyId
  ) {
    requireNonNull(varietyId, "apple variety id cannot be null");

    return repository.findById(varietyId.value())
        .map(AppleVarietyJdbcMapper::toVersionedAggregate);
  }
}
