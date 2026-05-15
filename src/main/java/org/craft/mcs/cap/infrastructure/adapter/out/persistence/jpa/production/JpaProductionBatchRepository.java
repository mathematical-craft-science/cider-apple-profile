package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.production;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.production.entity.ProductionBatchJpaMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


@Repository
@RequiredArgsConstructor
class JpaProductionBatchRepository
    implements ProductionBatchRepository {

  private final @NonNull SpringDataProductionBatchJpaRepository repository;

  @Override
  public @NonNull ProductionBatch save(@NonNull ProductionBatch productionBatch) {
    requireNonNull(productionBatch, "production batch cannot be null");

    final var entity = ProductionBatchJpaMapper.fromDomain(productionBatch);
    final var saved = repository.save(entity);

    return ProductionBatchJpaMapper.toDomain(saved);
  }
}
