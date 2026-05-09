package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock;

import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock.entity.FeedstockSampleJpaEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;


interface SpringDataFeedstockSampleQueryJpaRepository
    extends Repository<FeedstockSampleJpaEntity, @NonNull UUID> {

  @NonNull
  @Query("""
      select sample
      from FeedstockSampleJpaEntity sample
      where sample.batchId in (
        select batch.batchId
        from FeedstockBatchJpaEntity batch
        where batch.ciderMakerId = :ciderMakerId
      )
      """)
  List<@NonNull FeedstockSampleJpaEntity> findAllByCiderMakerId(
      @Param("ciderMakerId") @NonNull UUID ciderMakerId
  );

  @NonNull
  @Query("""
      select new org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock.FeedstockSampleSummaryJpaProjection(
        sample.sampleId,
        batch.ciderMakerId,
        sample.batchId,
        sample.name,
        sample.type,
        sample.measuredAt,
        sample.note
      )
      from FeedstockSampleJpaEntity sample, FeedstockBatchJpaEntity batch
      where sample.batchId = batch.batchId
        and sample.batchId = :batchId
      order by sample.measuredAt desc, sample.name asc
      """)
  List<@NonNull FeedstockSampleSummaryJpaProjection> findSummariesByBatchId(
      @Param("batchId") @NonNull UUID batchId
  );
}
