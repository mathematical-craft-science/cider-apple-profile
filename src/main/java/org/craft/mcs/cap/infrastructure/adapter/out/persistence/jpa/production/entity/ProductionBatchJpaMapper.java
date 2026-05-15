package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.production.entity;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class ProductionBatchJpaMapper {

  private ProductionBatchJpaMapper() {
  }

  public static @NonNull ProductionBatchJpaEntity fromDomain(@NonNull ProductionBatch batch) {
    requireNonNull(batch, "production batch cannot be null");

    final var entity = new ProductionBatchJpaEntity(
        batch.batchId().value(),
        batch.ciderMakerId().value(),
        batch.name().value(),
        batch.kind(),
        batch.status(),
        batch.sourceComposition().totalVolume().liters(),
        batch.profile().sugar().asBigDecimal(),
        batch.profile().acidity().asBigDecimal(),
        batch.profile().tannin() == null ? null : batch.profile().tannin().asBigDecimal(), // NOSONAR
        batch.targetDistance(),
        batch.note()
    );

    entity.replaceSources(batch.sourceComposition().sources()
        .stream()
        .map(ProductionBatchJpaMapper::sourceFromDomain)
        .toList());

    return entity;
  }

  public static @NonNull ProductionBatch toDomain(@NonNull ProductionBatchJpaEntity entity) {
    requireNonNull(entity, "production batch entity cannot be null");

    return ProductionBatch.restore(
        ProductionBatchId.of(requireNonNull(entity.batchId, "production batch id cannot be null")),
        CiderMakerId.of(requireNonNull(entity.ciderMakerId, "cider maker id cannot be null")),
        ProductionBatchName.of(requireNonNull(entity.name, "production batch name cannot be null")),
        requireNonNull(entity.kind, "production batch kind cannot be null"),
        requireNonNull(entity.status, "production batch status cannot be null"),
        new ProductionSourceComposition(
            entity.sources.stream()
                .map(ProductionBatchJpaMapper::sourceToDomain)
                .toList(),
            VolumeValue.ofLiters(requireNonNull(entity.totalVolumeLiters, "production total volume cannot be null"))
        ),
        new ProductionProfileSnapshot(
            SpecificGravityValue.of(requireNonNull(entity.sugarSpecificGravity, "production sugar cannot be null")),
            TitratableAcidityValue.of(requireNonNull(entity.acidityGLAsMalic, "production acidity cannot be null")),
            entity.tanninMgLTea == null ? null : TanninityValue.of(entity.tanninMgLTea) // NOSONAR
        ),
        entity.targetDistance,
        entity.note
    );
  }

  private static @NonNull ProductionBatchSourceJpaEntity sourceFromDomain(
      @NonNull ProductionSourceComponent source
  ) {
    requireNonNull(source, "production source cannot be null");
    return new ProductionBatchSourceJpaEntity(
        source.feedstockBatchId().value(),
        source.volume().liters()
    );
  }

  private static @NonNull ProductionSourceComponent sourceToDomain(
      @NonNull ProductionBatchSourceJpaEntity source
  ) {
    requireNonNull(source, "production source entity cannot be null");
    return new ProductionSourceComponent(
        FeedstockBatchId.of(requireNonNull(source.feedstockBatchId, "feedstock batch id cannot be null")),
        VolumeValue.ofLiters(requireNonNull(source.volumeLiters, "production source volume cannot be null"))
    );
  }
}
