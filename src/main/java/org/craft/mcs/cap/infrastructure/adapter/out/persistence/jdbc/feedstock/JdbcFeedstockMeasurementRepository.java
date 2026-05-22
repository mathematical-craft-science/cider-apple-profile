package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.entity.FeedstockMeasurementJdbcMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator.translateOptimisticLocking;


@Repository
@RequiredArgsConstructor
class JdbcFeedstockMeasurementRepository
    implements FeedstockMeasurementRepository {

  private final @NonNull SpringDataFeedstockMeasurementJdbcRepository repository;

  @Override
  public @NonNull FeedstockMeasurement save(@NonNull FeedstockMeasurement feedstockMeasurement) {
    requireNonNull(feedstockMeasurement, "feedstock measurement cannot be null");

    final var entity = repository.findById(feedstockMeasurement.measurementId().value())
        .map(existing -> FeedstockMeasurementJdbcMapper.updateFromDomain(feedstockMeasurement, existing))
        .orElseGet(() -> FeedstockMeasurementJdbcMapper.fromDomain(feedstockMeasurement));
    final var saved = translateOptimisticLocking("feedstock measurement", () -> repository.save(entity));

    return FeedstockMeasurementJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull Optional<@NonNull FeedstockMeasurement> findById(@NonNull FeedstockMeasurementId measurementId) {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");

    return repository.findById(measurementId.value())
        .map(FeedstockMeasurementJdbcMapper::toDomain);
  }

  @Override
  public void deleteById(@NonNull FeedstockMeasurementId measurementId) {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
    translateOptimisticLocking("feedstock measurement", () -> repository.deleteById(measurementId.value()));
  }
}
