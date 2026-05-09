package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSummary;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockSampleName;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock.entity.FeedstockSampleJpaMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


@Repository
@RequiredArgsConstructor
class JpaFeedstockSampleQueryRepository
    implements FeedstockSampleQueryRepository {

  private final @NonNull SpringDataFeedstockSampleQueryJpaRepository repository;

  @Override
  public @NonNull List<@NonNull FeedstockSample> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId) {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");

    return repository.findAllByCiderMakerId(ciderMakerId.value())
        .stream()
        .map(FeedstockSampleJpaMapper::toDomain)
        .toList();
  }

  @Override
  public @NonNull List<@NonNull FeedstockSampleSummary> findSummariesByBatchId(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    return repository.findSummariesByBatchId(batchId.value())
        .stream()
        .map(JpaFeedstockSampleQueryRepository::toSummary)
        .toList();
  }

  private static @NonNull FeedstockSampleSummary toSummary(
      @NonNull FeedstockSampleSummaryJpaProjection projection
  ) {
    return new FeedstockSampleSummary(
        FeedstockSampleId.of(projection.sampleId()),
        CiderMakerId.of(projection.ciderMakerId()),
        FeedstockBatchId.of(projection.batchId()),
        FeedstockSampleName.of(projection.name()),
        projection.type(),
        projection.measuredAt(),
        projection.note()
    );
  }
}
