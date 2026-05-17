package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.port.out.LatestFeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock.entity.FeedstockMeasurementJpaMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


@Repository
@RequiredArgsConstructor
class JpaFeedstockMeasurementQueryRepository
    implements FeedstockMeasurementQueryRepository {

  private static final @NonNull LocalDate NO_SEASON_DATE = LocalDate.of(1970, 1, 1);
  private static final @NonNull Instant NO_CURSOR_MEASURED_AT = Instant.EPOCH;
  private static final @NonNull UUID NO_CURSOR_MEASUREMENT_ID = new UUID(0L, 0L);

  private final @NonNull SpringDataFeedstockMeasurementQueryJpaRepository repository;

  @Override
  public @NonNull List<@NonNull FeedstockMeasurement> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId) {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");

    return repository.findAllByCiderMakerId(ciderMakerId.value())
        .stream()
        .map(FeedstockMeasurementJpaMapper::toDomain)
        .toList();
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurement> findAllByCiderMakerIdAndSeason(
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
        .map(FeedstockMeasurementJpaMapper::toDomain)
        .toList();
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurement> findSugarAcidityPointCandidates(
      @NonNull FeedstockMeasurementSugarAcidityPointCandidateQuery query
  ) {
    requireNonNull(query, "feedstock measurement point candidate query cannot be null");

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
            season == null ? NO_SEASON_DATE : season.startsOn(),
            season == null ? NO_SEASON_DATE : season.endsOn(),
            cursor != null,
            cursor == null ? NO_CURSOR_MEASURED_AT : cursor.measuredAt(),
            cursor == null ? NO_CURSOR_MEASUREMENT_ID : cursor.measurementId(),
            PageRequest.ofSize(query.limit())
        )
        .stream()
        .map(FeedstockMeasurementJpaMapper::toDomain)
        .toList();
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurement> findLatestSugarAcidityPointCandidates(
      @NonNull LatestFeedstockMeasurementSugarAcidityPointCandidateQuery query
  ) {
    requireNonNull(query, "latest feedstock measurement point candidate query cannot be null");

    final var season = query.season();
    final var cursor = query.after();
    final var batchIds = query.batch() == null
        ? List.of(new UUID(0L, 0L))
        : query.batch().batchIds().stream()
            .map(FeedstockBatchId::value)
            .toList();

    return repository.findLatestSugarAcidityPointCandidates(
            query.ciderMakerId().value(),
            query.batch() != null,
            batchIds,
            season != null,
            season == null ? NO_SEASON_DATE : season.startsOn(),
            season == null ? NO_SEASON_DATE : season.endsOn(),
            cursor != null,
            cursor == null ? NO_CURSOR_MEASURED_AT : cursor.measuredAt(),
            cursor == null ? NO_CURSOR_MEASUREMENT_ID : cursor.measurementId(),
            PageRequest.ofSize(query.limit())
        )
        .stream()
        .map(FeedstockMeasurementJpaMapper::toDomain)
        .toList();
  }

  @Override
  public @NonNull Optional<@NonNull FeedstockMeasurement> findLatestByBatchId(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    return repository.findLatestByBatchId(batchId.value(), PageRequest.ofSize(1))
        .stream()
        .findFirst()
        .map(FeedstockMeasurementJpaMapper::toDomain);
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurementSummary> findSummariesByBatchId(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    return repository.findSummariesByBatchId(batchId.value())
        .stream()
        .map(JpaFeedstockMeasurementQueryRepository::toSummary)
        .toList();
  }

  private static @NonNull FeedstockMeasurementSummary toSummary(
      @NonNull FeedstockMeasurementSummaryJpaProjection projection
  ) {
    final var measurement = FeedstockMeasurementJpaMapper.toDomain(projection.measurement());
    return new FeedstockMeasurementSummary(
        measurement.measurementId(),
        CiderMakerId.of(projection.ciderMakerId()),
        measurement.batchId(),
        measurement.name(),
        measurement.measuredAt(),
        measurement.chemicalProfile(),
        measurement.note()
    );
  }
}
