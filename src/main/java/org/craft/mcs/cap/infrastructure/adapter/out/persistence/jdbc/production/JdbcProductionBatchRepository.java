package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.entity.ProductionBatchJdbcMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator.translateOptimisticLocking;


@Repository
@RequiredArgsConstructor
class JdbcProductionBatchRepository
    implements ProductionBatchRepository {

  private final @NonNull SpringDataProductionBatchJdbcRepository repository;

  @Override
  public @NonNull ProductionBatch save(@NonNull ProductionBatch productionBatch) {
    requireNonNull(productionBatch, "production batch cannot be null");

    final var entity = repository.findById(productionBatch.batchId().value())
        .map(existing -> ProductionBatchJdbcMapper.updateFromDomain(productionBatch, existing))
        .orElseGet(() -> ProductionBatchJdbcMapper.fromDomain(productionBatch));
    final var saved = translateOptimisticLocking("production batch", () -> repository.save(entity));

    return ProductionBatchJdbcMapper.toDomain(saved);
  }
}
