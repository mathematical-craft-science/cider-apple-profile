package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.entity;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionTargetDistance;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class ProductionBatchJdbcMapper {

  private ProductionBatchJdbcMapper() {
  }

  public static @NonNull ProductionBatchJdbcEntity fromDomain(@NonNull ProductionBatch batch) {
    requireNonNull(batch, "production batch cannot be null");

    final var profile = batch.profile();
    return new ProductionBatchJdbcEntity(
        batch.batchId().value(),
        batch.ciderMakerId().value(),
        batch.name().value(),
        batch.kind(),
        batch.status(),
        batch.volume().liters(),
        profile.sugar().asBigDecimal(),
        profile.acidity().asBigDecimal(),
        tanninValue(profile),
        yeastAssimilableNitrogenValue(profile),
        targetDistanceValue(batch.targetDistance()),
        batch.note()
    );
  }

  public static @NonNull ProductionBatchJdbcEntity updateFromDomain(
      @NonNull ProductionBatch batch,
      @NonNull ProductionBatchJdbcEntity entity
  ) {
    requireNonNull(batch, "production batch cannot be null");
    requireNonNull(entity, "production batch row cannot be null");

    final var updated = fromDomain(batch);

    entity.ciderMakerId = updated.ciderMakerId;
    entity.name = updated.name;
    entity.kind = updated.kind;
    entity.status = updated.status;
    entity.totalVolumeLiters = updated.totalVolumeLiters;
    entity.sugarSpecificGravity = updated.sugarSpecificGravity;
    entity.acidityGLAsMalic = updated.acidityGLAsMalic;
    entity.tanninMgLTea = updated.tanninMgLTea;
    entity.yeastAssimilableNitrogenMgNL = updated.yeastAssimilableNitrogenMgNL;
    entity.targetDistance = updated.targetDistance;
    entity.note = updated.note;
    return entity;
  }

  public static @NonNull ProductionBatch toDomain(@NonNull ProductionBatchJdbcEntity entity) {
    requireNonNull(entity, "production batch row cannot be null");

    return ProductionBatch.restore(
        ProductionBatchId.of(requireNonNull(entity.batchId, "production batch id cannot be null")),
        CiderMakerId.of(requireNonNull(entity.ciderMakerId, "cider maker id cannot be null")),
        ProductionBatchName.of(requireNonNull(entity.name, "production batch name cannot be null")),
        requireNonNull(entity.kind, "production batch kind cannot be null"),
        requireNonNull(entity.status, "production batch status cannot be null"),
        VolumeValue.ofLiters(requireNonNull(entity.totalVolumeLiters, "production total volume cannot be null")),
        new ProductionProfileSnapshot(
            SpecificGravityValue.of(requireNonNull(entity.sugarSpecificGravity, "production sugar cannot be null")),
            TitratableAcidityValue.of(requireNonNull(entity.acidityGLAsMalic, "production acidity cannot be null")),
            toTanninValue(entity.tanninMgLTea),
            toYeastAssimilableNitrogenValue(entity.yeastAssimilableNitrogenMgNL)
        ),
        toTargetDistance(entity.targetDistance),
        entity.note
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

  private static @Nullable BigDecimal targetDistanceValue(@Nullable ProductionTargetDistance targetDistance) {
    return targetDistance == null ? null : targetDistance.value();
  }

  private static @Nullable TanninityValue toTanninValue(@Nullable BigDecimal value) {
    return value == null ? null : TanninityValue.of(value);
  }

  private static @Nullable YeastAssimilableNitrogenValue toYeastAssimilableNitrogenValue(@Nullable BigDecimal value) {
    return value == null ? null : YeastAssimilableNitrogenValue.of(value);
  }

  private static @Nullable ProductionTargetDistance toTargetDistance(@Nullable BigDecimal value) {
    return value == null ? null : ProductionTargetDistance.of(value);
  }

}
