package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.entity.ProductionBatchJdbcMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


@Repository
@RequiredArgsConstructor
class JdbcProductionBatchRepository
    implements ProductionBatchRepository {

  private final @NonNull SpringDataProductionBatchJdbcRepository repository;

  @Override
  public @NonNull ProductionBatch save(@NonNull ProductionBatch productionBatch) {
    requireNonNull(productionBatch, "production batch cannot be null");

    final var entity = ProductionBatchJdbcMapper.fromDomain(productionBatch);
    final var saved = repository.save(entity);

    return ProductionBatchJdbcMapper.toDomain(saved);
  }
}
