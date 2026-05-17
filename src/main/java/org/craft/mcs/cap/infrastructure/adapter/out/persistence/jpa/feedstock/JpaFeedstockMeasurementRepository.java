package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock.entity.FeedstockMeasurementJpaMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


@Repository
@RequiredArgsConstructor
class JpaFeedstockMeasurementRepository
    implements FeedstockMeasurementRepository {

  private final @NonNull SpringDataFeedstockMeasurementJpaRepository repository;

  @Override
  public @NonNull FeedstockMeasurement save(@NonNull FeedstockMeasurement feedstockMeasurement) {
    requireNonNull(feedstockMeasurement, "feedstock measurement cannot be null");

    final var entity = FeedstockMeasurementJpaMapper.fromDomain(feedstockMeasurement);
    final var saved = repository.save(entity);

    return FeedstockMeasurementJpaMapper.toDomain(saved);
  }

  @Override
  public @NonNull Optional<@NonNull FeedstockMeasurement> findById(@NonNull FeedstockMeasurementId measurementId) {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");

    return repository.findById(measurementId.value())
        .map(FeedstockMeasurementJpaMapper::toDomain);
  }

  @Override
  public void deleteById(@NonNull FeedstockMeasurementId measurementId) {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
    repository.deleteById(measurementId.value());
  }
}
