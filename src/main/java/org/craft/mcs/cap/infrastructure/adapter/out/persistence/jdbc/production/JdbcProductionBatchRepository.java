package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.entity.ProductionBatchJdbcMapper;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import java.util.Optional;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator.translateOptimisticLocking;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcVersionedUpdate.complete;


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
    final var profile = aggregate.initialProfileSnapshot();
    final var affectedRows = translateOptimisticLocking(
        "production batch",
        () -> repository.updateByIdAndVersion(
            aggregate.batchId().value(),
            aggregate.name().value(),
            aggregate.vesselId().value(),
            aggregate.lifecycle(),
            aggregate.alcoholicFermentationState(),
            aggregate.currentVolume().liters(),
            aggregate.currentMaterialStateId().value(),
            profile.specificGravity().asBigDecimal(),
            profile.titratableAcidity().asBigDecimal(),
            profile.tannin() == null ? null : profile.tannin().asBigDecimal(),
            profile.yeastAssimilableNitrogen() == null
                ? null
                : profile.yeastAssimilableNitrogen().asBigDecimal(),
            aggregate.note(),
            productionBatch.revision().value()
        )
    );
    return complete("production batch", productionBatch, affectedRows);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<ProductionBatch>> findVersionedById(
      @NonNull ProductionBatchId batchId
  ) {
    requireNonNull(batchId, "production batch id cannot be null");
    return repository.findById(batchId.value()).map(ProductionBatchJdbcMapper::toVersionedAggregate);
  }

}
