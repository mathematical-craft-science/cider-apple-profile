package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.entity.FeedstockBatchJdbcEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


interface SpringDataFeedstockBatchJdbcRepository
    extends ListCrudRepository<FeedstockBatchJdbcEntity, @NonNull UUID> {

  @Query("""
      SELECT *
      FROM feedstock_batches
      WHERE batch_id = :batchId
        AND deleted_at IS NULL
      FOR UPDATE
      """)
  @NonNull
  Optional<@NonNull FeedstockBatchJdbcEntity> findByIdForConsumption(
      @Param("batchId") @NonNull UUID batchId
  );

  @NonNull
  List<@NonNull FeedstockBatchJdbcEntity> findAllByWorkspaceIdAndDeletedAtIsNullOrderByCreatedAtAscBatchIdAsc(
      @NonNull UUID workspaceId
  );

  @Modifying
  @Query("""
      UPDATE feedstock_batches
      SET deleted_at = CURRENT_TIMESTAMP,
          updated_at = CURRENT_TIMESTAMP,
          version = version + 1
      WHERE batch_id = :batchId
        AND deleted_at IS NULL
        AND version = :version
      """)
  int softDeleteByIdAndVersion(
      @Param("batchId") @NonNull UUID batchId,
      @Param("version") long version
  );
}
