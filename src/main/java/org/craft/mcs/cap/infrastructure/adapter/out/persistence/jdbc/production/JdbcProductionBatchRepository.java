package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
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

  @Override
  public @NonNull Optional<@NonNull ProductionBatch> findById(@NonNull ProductionBatchId batchId) {
    requireNonNull(batchId, "production batch id cannot be null");

    return repository.findById(batchId.value())
        .map(ProductionBatchJdbcMapper::toDomain);
  }

  @Override
  public @NonNull List<@NonNull ProductionBatch> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId) {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");

    return repository.findAllByCiderMakerIdOrderByCreatedAtDescBatchIdDesc(ciderMakerId.value())
        .stream()
        .map(ProductionBatchJdbcMapper::toDomain)
        .toList();
  }
}
