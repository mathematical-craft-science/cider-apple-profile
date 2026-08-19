package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.batch;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.core.production.batch.domain.model.value.AlcoholicFermentationState;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.batch.entity.ProductionBatchJdbcEntity;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


interface SpringDataProductionBatchJdbcRepository
    extends ListCrudRepository<ProductionBatchJdbcEntity, @NonNull UUID> {

  @Query("""
      SELECT batch_id,
             workspace_id,
             name,
             vessel_id,
             lifecycle,
             alcoholic_fermentation_state,
             current_volume_liters,
             current_material_state_id,
             initial_sugar_specific_gravity,
             initial_acidity_g_l_as_malic,
             initial_tannin_mg_l_tae,
             initial_yan_mg_n_l,
             note,
             version,
             created_at,
             updated_at
        FROM production_batches
       WHERE batch_id = :batchId
         FOR SHARE
      """)
  @NonNull Optional<@NonNull ProductionBatchJdbcEntity> lockForMeasurementChange(
      @Param("batchId") @NonNull UUID batchId
  );

  @Modifying
  @Query("""
      UPDATE production_batches
         SET name = :name,
             vessel_id = :vesselId,
             lifecycle = :lifecycle,
             alcoholic_fermentation_state = :alcoholicFermentationState,
             current_volume_liters = :currentVolumeLiters,
             current_material_state_id = :currentMaterialStateId,
             initial_sugar_specific_gravity = :initialSpecificGravity,
             initial_acidity_g_l_as_malic = :initialTitratableAcidity,
             initial_tannin_mg_l_tae = :initialTannin,
             initial_yan_mg_n_l = :initialYan,
             note = :note,
             version = version + 1,
             updated_at = GREATEST(clock_timestamp(), created_at, updated_at)
       WHERE batch_id = :batchId
         AND version = :expectedVersion
      """)
  @SuppressWarnings("java:S107")
  int updateByIdAndVersion(
      @Param("batchId") @NonNull UUID batchId,
      @Param("name") @NonNull String name,
      @Param("vesselId") @NonNull UUID vesselId,
      @Param("lifecycle") @NonNull BatchLifecycle lifecycle,
      @Param("alcoholicFermentationState") @NonNull AlcoholicFermentationState alcoholicFermentationState,
      @Param("currentVolumeLiters") @NonNull BigDecimal currentVolumeLiters,
      @Param("currentMaterialStateId") @NonNull UUID currentMaterialStateId,
      @Param("initialSpecificGravity") @NonNull BigDecimal initialSpecificGravity,
      @Param("initialTitratableAcidity") @NonNull BigDecimal initialTitratableAcidity,
      @Param("initialTannin") @Nullable BigDecimal initialTannin,
      @Param("initialYan") @Nullable BigDecimal initialYan,
      @Param("note") @Nullable String note,
      @Param("expectedVersion") long expectedVersion
  );

  @NonNull
  List<@NonNull ProductionBatchJdbcEntity> findAllByWorkspaceIdOrderByCreatedAtDescBatchIdDesc(
      @NonNull UUID workspaceId
  );

  @NonNull
  List<@NonNull ProductionBatchJdbcEntity> findAllByBatchIdInAndWorkspaceId(
      @NonNull List<@NonNull UUID> batchIds,
      @NonNull UUID workspaceId
  );
}
