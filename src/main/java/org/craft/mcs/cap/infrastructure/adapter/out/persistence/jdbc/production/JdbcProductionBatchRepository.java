package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.entity.ProductionBatchJdbcMapper;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import java.util.Optional;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator.translateOptimisticLocking;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcAggregateRevisionGuard.requireExpectedRevision;


@Repository
@RequiredArgsConstructor
class JdbcProductionBatchRepository
    implements ProductionBatchRepository {

  private final @NonNull SpringDataProductionBatchJdbcRepository repository;

  @Override
  public @NonNull ProductionBatch create(@NonNull ProductionBatch productionBatch) {
    requireNonNull(productionBatch, "production batch cannot be null");

    final var saved = translateOptimisticLocking(
        "production batch",
        () -> repository.save(ProductionBatchJdbcMapper.fromDomain(productionBatch))
    );

    return ProductionBatchJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull VersionedAggregate<ProductionBatch> update(
      @NonNull VersionedAggregate<ProductionBatch> productionBatch
  ) {
    requireNonNull(productionBatch, "versioned production batch cannot be null");
    final var aggregate = productionBatch.aggregate();
    final var entity = repository.findById(aggregate.batchId().value())
        .orElseThrow(() -> new ConcurrentResourceModificationException("production batch", null));
    requireExpectedRevision(
        "production batch",
        productionBatch.revision(),
        ProductionBatchJdbcMapper.revision(entity)
    );
    final var saved = translateOptimisticLocking(
        "production batch",
        () -> repository.save(ProductionBatchJdbcMapper.updateFromDomain(aggregate, entity))
    );
    return ProductionBatchJdbcMapper.toVersionedAggregate(saved);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<ProductionBatch>> findVersionedById(
      @NonNull ProductionBatchId batchId
  ) {
    requireNonNull(batchId, "production batch id cannot be null");
    return repository.findById(batchId.value()).map(ProductionBatchJdbcMapper::toVersionedAggregate);
  }

}
