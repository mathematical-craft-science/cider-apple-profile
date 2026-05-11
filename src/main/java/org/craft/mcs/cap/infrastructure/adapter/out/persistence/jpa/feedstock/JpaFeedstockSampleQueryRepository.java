package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSummary;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockSampleName;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock.entity.FeedstockSampleJpaMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.PageRequest;
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
  public @NonNull List<@NonNull FeedstockSample> findAllByCiderMakerIdAndSeason(
      @NonNull CiderMakerId ciderMakerId,
      @NonNull FeedstockSeason season
  ) {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(season, "feedstock season cannot be null");

    return repository.findAllByCiderMakerIdAndSeason(
            ciderMakerId.value(),
            season.startsOn(),
            season.endsOn()
        )
        .stream()
        .map(FeedstockSampleJpaMapper::toDomain)
        .toList();
  }

  @Override
  public @NonNull List<@NonNull FeedstockSample> findSugarAcidityPointCandidates(
      @NonNull FeedstockSampleSugarAcidityPointCandidateQuery query
  ) {
    requireNonNull(query, "feedstock sample point candidate query cannot be null");

    final var cursor = query.after();
    final var season = query.season();
    final var batchIds = query.batch() == null
        ? List.of(new UUID(0L, 0L))
        : query.batch().batchIds().stream()
            .map(FeedstockBatchId::value)
            .toList();

    return repository.findSugarAcidityPointCandidates(
            query.ciderMakerId().value(),
            query.batch() != null,
            batchIds,
            season != null,
            season == null ? null : season.startsOn(),
            season == null ? null : season.endsOn(),
            cursor == null ? null : cursor.measuredAt(),
            cursor == null ? null : cursor.sampleId(),
            PageRequest.ofSize(query.limit())
        )
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
