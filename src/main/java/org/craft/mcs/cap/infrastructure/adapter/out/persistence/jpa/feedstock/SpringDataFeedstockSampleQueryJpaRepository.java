package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock;

import java.util.List;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock.entity.FeedstockSampleJpaEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
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
      select sample
      from FeedstockSampleJpaEntity sample, FeedstockBatchJpaEntity batch
      where sample.batchId = batch.batchId
        and batch.ciderMakerId = :ciderMakerId
        and batch.seasonStartsOn <= :seasonEndsOn
        and batch.seasonEndsOn >= :seasonStartsOn
      """)
  List<@NonNull FeedstockSampleJpaEntity> findAllByCiderMakerIdAndSeason(
      @Param("ciderMakerId") @NonNull UUID ciderMakerId,
      @Param("seasonStartsOn") @NonNull LocalDate seasonStartsOn,
      @Param("seasonEndsOn") @NonNull LocalDate seasonEndsOn
  );

  @NonNull
  @Query("""
      select sample
      from FeedstockSampleJpaEntity sample, FeedstockBatchJpaEntity batch
      where sample.batchId = batch.batchId
        and batch.ciderMakerId = :ciderMakerId
        and (:filterByBatch = false or sample.batchId in :batchIds)
        and (
          :filterBySeason = false
          or (batch.seasonStartsOn <= :seasonEndsOn and batch.seasonEndsOn >= :seasonStartsOn)
        )
        and (
          :afterMeasuredAt is null
          or sample.measuredAt > :afterMeasuredAt
          or (sample.measuredAt = :afterMeasuredAt and sample.sampleId > :afterSampleId)
        )
      order by sample.measuredAt asc, sample.sampleId asc
      """)
  List<@NonNull FeedstockSampleJpaEntity> findSugarAcidityPointCandidates(
      @Param("ciderMakerId") @NonNull UUID ciderMakerId,
      @Param("filterByBatch") boolean filterByBatch,
      @Param("batchIds") @NonNull List<@NonNull UUID> batchIds,
      @Param("filterBySeason") boolean filterBySeason,
      @Param("seasonStartsOn") @Nullable LocalDate seasonStartsOn,
      @Param("seasonEndsOn") @Nullable LocalDate seasonEndsOn,
      @Param("afterMeasuredAt") @Nullable Instant afterMeasuredAt,
      @Param("afterSampleId") @Nullable UUID afterSampleId,
      @NonNull Pageable pageable
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
