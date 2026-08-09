package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.entity.FeedstockBatchJdbcEntity;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


interface SpringDataFeedstockBatchJdbcRepository
    extends ListCrudRepository<FeedstockBatchJdbcEntity, @NonNull UUID> {

  @Modifying
  @Query("""
      UPDATE feedstock_batches
         SET name = :name,
             vessel_id = :vesselId,
             type = :type,
             season_starts_on = :seasonStartsOn,
             season_ends_on = :seasonEndsOn,
             source = :source,
             lifecycle = :lifecycle,
             available_volume_liters = :availableVolumeLiters,
             current_material_state_id = :currentMaterialStateId,
             note = :note,
             deleted_at = :deletedAt,
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
      @Param("type") @NonNull FeedstockType type,
      @Param("seasonStartsOn") @NonNull LocalDate seasonStartsOn,
      @Param("seasonEndsOn") @NonNull LocalDate seasonEndsOn,
      @Param("source") @Nullable String source,
      @Param("lifecycle") @NonNull BatchLifecycle lifecycle,
      @Param("availableVolumeLiters") @NonNull BigDecimal availableVolumeLiters,
      @Param("currentMaterialStateId") @NonNull UUID currentMaterialStateId,
      @Param("note") @Nullable String note,
      @Param("deletedAt") @Nullable Instant deletedAt,
      @Param("expectedVersion") long expectedVersion
  );

  @NonNull
  Optional<@NonNull FeedstockBatchJdbcEntity> findByBatchIdAndDeletedAtIsNull(
      @NonNull UUID batchId
  );

  @NonNull
  List<@NonNull FeedstockBatchJdbcEntity> findAllByWorkspaceIdAndDeletedAtIsNullOrderByCreatedAtAscBatchIdAsc(
      @NonNull UUID workspaceId
  );

  @NonNull
  List<@NonNull FeedstockBatchJdbcEntity> findAllByBatchIdInAndWorkspaceIdAndDeletedAtIsNull(
      @NonNull List<@NonNull UUID> batchIds,
      @NonNull UUID workspaceId
  );

}
