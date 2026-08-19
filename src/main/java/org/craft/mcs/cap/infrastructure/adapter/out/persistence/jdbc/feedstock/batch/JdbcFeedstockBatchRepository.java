package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.entity.FeedstockBatchJdbcMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator.translateOptimisticLocking;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcVersionedUpdate.complete;


@Repository
@RequiredArgsConstructor
class JdbcFeedstockBatchRepository
    implements FeedstockBatchRepository {

  private final @NonNull SpringDataFeedstockBatchJdbcRepository repository;

  @Override
  public @NonNull FeedstockBatch create(@NonNull FeedstockBatch feedstockBatch) {
    requireNonNull(feedstockBatch, "feedstock batch cannot be null");

    final var saved = translateOptimisticLocking(
        "feedstock batch",
        () -> repository.save(FeedstockBatchJdbcMapper.fromDomain(feedstockBatch))
    );

    return FeedstockBatchJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull VersionedAggregate<FeedstockBatch> update(
      @NonNull VersionedAggregate<FeedstockBatch> feedstockBatch
  ) {
    requireNonNull(feedstockBatch, "versioned feedstock batch cannot be null");
    final var aggregate = feedstockBatch.aggregate();
    final var affectedRows = translateOptimisticLocking(
        "feedstock batch",
        () -> repository.updateByIdAndVersion(
            aggregate.batchId().value(),
            aggregate.name().value(),
            aggregate.vesselId().value(),
            aggregate.type(),
            aggregate.season().startsOn(),
            aggregate.season().endsOn(),
            aggregate.source() == null ? null : aggregate.source().value(),
            aggregate.lifecycle(),
            aggregate.availableVolume().liters(),
            aggregate.currentMaterialStateId().value(),
            aggregate.note(),
            aggregate.deletedAt(),
            feedstockBatch.revision().value()
        )
    );
    return complete("feedstock batch", feedstockBatch, affectedRows);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<FeedstockBatch>> findVersionedById(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    return repository.findByBatchIdAndDeletedAtIsNull(batchId.value())
        .map(FeedstockBatchJdbcMapper::toVersionedAggregate);
  }

  @Override
  public @NonNull Optional<@NonNull VersionedAggregate<FeedstockBatch>> lockVersionedById(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    return repository.lockById(batchId.value())
        .map(FeedstockBatchJdbcMapper::toVersionedAggregate);
  }

  @Override
  public @NonNull Optional<@NonNull FeedstockBatch> lockForMeasurementChange(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    return repository.lockForMeasurementChange(batchId.value())
        .map(FeedstockBatchJdbcMapper::toDomain);
  }
}
