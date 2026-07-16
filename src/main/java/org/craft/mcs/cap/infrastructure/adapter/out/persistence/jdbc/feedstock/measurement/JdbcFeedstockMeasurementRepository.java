package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.measurement;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.measurement.entity.FeedstockMeasurementJdbcMapper;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator.translateOptimisticLocking;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcAggregateRevisionGuard.requireExpectedRevision;


@Repository
@RequiredArgsConstructor
class JdbcFeedstockMeasurementRepository
    implements FeedstockMeasurementRepository {

  private final @NonNull SpringDataFeedstockMeasurementJdbcRepository repository;

  @Override
  public @NonNull FeedstockMeasurement create(@NonNull FeedstockMeasurement feedstockMeasurement) {
    requireNonNull(feedstockMeasurement, "feedstock measurement cannot be null");

    final var saved = translateOptimisticLocking(
        "feedstock measurement",
        () -> repository.save(FeedstockMeasurementJdbcMapper.fromDomain(feedstockMeasurement))
    );

    return FeedstockMeasurementJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull VersionedAggregate<FeedstockMeasurement> update(
      @NonNull VersionedAggregate<FeedstockMeasurement> measurement
  ) {
    requireNonNull(measurement, "versioned feedstock measurement cannot be null");
    final var aggregate = measurement.aggregate();
    final var entity = repository.findById(aggregate.measurementId().value())
        .orElseThrow(() -> new ConcurrentResourceModificationException("feedstock measurement", null));
    requireExpectedRevision(
        "feedstock measurement",
        measurement.revision(),
        FeedstockMeasurementJdbcMapper.revision(entity)
    );
    final var saved = translateOptimisticLocking(
        "feedstock measurement",
        () -> repository.save(FeedstockMeasurementJdbcMapper.updateFromDomain(aggregate, entity))
    );
    return FeedstockMeasurementJdbcMapper.toVersionedAggregate(saved);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<FeedstockMeasurement>> findVersionedById(
      @NonNull FeedstockMeasurementId measurementId
  ) {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");

    return repository.findById(measurementId.value())
        .map(FeedstockMeasurementJdbcMapper::toVersionedAggregate);
  }

  @Override
  public void delete(@NonNull VersionedAggregate<FeedstockMeasurement> measurement) {
    requireNonNull(measurement, "versioned feedstock measurement cannot be null");
    final var affectedRows = repository.deleteByIdAndVersion(
        measurement.aggregate().measurementId().value(),
        measurement.revision().value()
    );
    if (affectedRows != 1) {
      throw new ConcurrentResourceModificationException("feedstock measurement", null);
    }
  }
}
