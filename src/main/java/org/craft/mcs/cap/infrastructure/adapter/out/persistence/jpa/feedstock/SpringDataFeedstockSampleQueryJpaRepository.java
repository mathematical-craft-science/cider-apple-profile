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
}
