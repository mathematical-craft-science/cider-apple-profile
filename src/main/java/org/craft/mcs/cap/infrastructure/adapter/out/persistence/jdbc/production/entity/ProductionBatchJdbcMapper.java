package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.entity;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.production.application.model.ProductionBatchView;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class ProductionBatchJdbcMapper {

  private ProductionBatchJdbcMapper() {
  }

  public static @NonNull ProductionBatchJdbcEntity fromDomain(@NonNull ProductionBatch batch) {
    requireNonNull(batch, "production batch cannot be null");

    final var profile = batch.initialProfileSnapshot();
    return new ProductionBatchJdbcEntity(
        batch.batchId().value(),
        batch.workspaceId().value(),
        batch.name().value(),
        batch.vesselId().value(),
        batch.lifecycle(),
        batch.alcoholicFermentationState(),
        batch.currentVolume().liters(),
        batch.currentMaterialStateId().value(),
        profile.specificGravity().asBigDecimal(),
        profile.titratableAcidity().asBigDecimal(),
        tanninValue(profile),
        yeastAssimilableNitrogenValue(profile),
        batch.note()
    );
  }

  public static @NonNull ProductionBatch toDomain(@NonNull ProductionBatchJdbcEntity entity) {
    requireNonNull(entity, "production batch row cannot be null");

    return ProductionBatch.restore(
        ProductionBatchId.of(requireNonNull(entity.batchId, "production batch id cannot be null")),
        WorkspaceId.of(requireNonNull(entity.workspaceId, "workspace id cannot be null")),
        BatchName.of(requireNonNull(entity.name, "production batch name cannot be null")),
        VesselId.of(requireNonNull(entity.vesselId, "production vessel id cannot be null")),
        requireNonNull(entity.lifecycle, "production batch lifecycle cannot be null"),
        requireNonNull(entity.alcoholicFermentationState, "alcoholic fermentation state cannot be null"),
        VolumeValue.ofLiters(requireNonNull(entity.currentVolumeLiters, "production current volume cannot be null")),
        MaterialStateId.of(requireNonNull(
            entity.currentMaterialStateId,
            "production current material state id cannot be null"
        )),
        toProfile(entity),
        entity.note
    );
  }

  public static @NonNull VersionedAggregate<ProductionBatch> toVersionedAggregate(
      @NonNull ProductionBatchJdbcEntity entity
  ) {
    return new VersionedAggregate<>(toDomain(entity), AggregateRevision.of(entity.version));
  }

  public static @NonNull ProductionBatchView toView(@NonNull ProductionBatchJdbcEntity entity) {
    requireNonNull(entity, "production batch row cannot be null");
    return new ProductionBatchView(
        ProductionBatchId.of(requireNonNull(entity.batchId, "production batch id cannot be null")),
        WorkspaceId.of(requireNonNull(entity.workspaceId, "workspace id cannot be null")),
        BatchName.of(requireNonNull(entity.name, "production batch name cannot be null")),
        VesselId.of(requireNonNull(entity.vesselId, "production vessel id cannot be null")),
        requireNonNull(entity.lifecycle, "production batch lifecycle cannot be null"),
        requireNonNull(entity.alcoholicFermentationState, "alcoholic fermentation state cannot be null"),
        VolumeValue.ofLiters(requireNonNull(entity.currentVolumeLiters, "production current volume cannot be null")),
        MaterialStateId.of(requireNonNull(
            entity.currentMaterialStateId,
            "production current material state id cannot be null"
        )),
        toProfile(entity),
        entity.note
    );
  }

  private static @NonNull ProductionProfileSnapshot toProfile(
      @NonNull ProductionBatchJdbcEntity entity
  ) {
    return new ProductionProfileSnapshot(
        SpecificGravityValue.of(
            requireNonNull(entity.initialSpecificGravity, "production initial specific gravity cannot be null")
        ),
        TitratableAcidityValue.of(
            requireNonNull(
                entity.initialTitratableAcidityGLAsMalic,
                "production initial titratable acidity cannot be null"
            )
        ),
        toTanninValue(entity.initialTanninMgLTae),
        toYeastAssimilableNitrogenValue(entity.initialYeastAssimilableNitrogenMgNL)
    );
  }

  private static @Nullable BigDecimal tanninValue(@NonNull ProductionProfileSnapshot profile) {
    final var tannin = profile.tannin();
    return tannin == null ? null : tannin.asBigDecimal();
  }

  private static @Nullable BigDecimal yeastAssimilableNitrogenValue(@NonNull ProductionProfileSnapshot profile) {
    final var yeastAssimilableNitrogen = profile.yeastAssimilableNitrogen();
    return yeastAssimilableNitrogen == null ? null : yeastAssimilableNitrogen.asBigDecimal();
  }

  private static @Nullable TanninValue toTanninValue(@Nullable BigDecimal value) {
    return value == null ? null : TanninValue.of(value);
  }

  private static @Nullable YeastAssimilableNitrogenValue toYeastAssimilableNitrogenValue(@Nullable BigDecimal value) {
    return value == null ? null : YeastAssimilableNitrogenValue.of(value);
  }

}
