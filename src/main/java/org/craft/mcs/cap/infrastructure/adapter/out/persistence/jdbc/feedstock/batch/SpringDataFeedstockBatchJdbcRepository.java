package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch;

import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.entity.FeedstockBatchJdbcEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


interface SpringDataFeedstockBatchJdbcRepository
    extends ListCrudRepository<FeedstockBatchJdbcEntity, @NonNull UUID> {

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
      """)
  void softDeleteById(@Param("batchId") @NonNull UUID batchId);
}
