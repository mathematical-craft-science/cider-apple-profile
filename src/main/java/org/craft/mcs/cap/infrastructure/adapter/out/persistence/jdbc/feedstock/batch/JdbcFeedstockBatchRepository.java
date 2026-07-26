package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.entity.FeedstockBatchJdbcMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator.translateOptimisticLocking;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcAggregateRevisionGuard.requireExpectedRevision;


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
    final var entity = repository.findById(aggregate.batchId().value())
        .orElseThrow(() -> new ConcurrentResourceModificationException("feedstock batch", null));
    requireExpectedRevision(
        "feedstock batch",
        feedstockBatch.revision(),
        FeedstockBatchJdbcMapper.revision(entity)
    );
    final var saved = translateOptimisticLocking(
        "feedstock batch",
        () -> repository.save(FeedstockBatchJdbcMapper.updateFromDomain(aggregate, entity))
    );
    return FeedstockBatchJdbcMapper.toVersionedAggregate(saved);
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
  public void delete(@NonNull VersionedAggregate<FeedstockBatch> feedstockBatch) {
    requireNonNull(feedstockBatch, "feedstock batch cannot be null");
    final var affectedRows = repository.softDeleteByIdAndVersion(
        feedstockBatch.aggregate().batchId().value(),
        feedstockBatch.revision().value()
    );
    if (affectedRows != 1) {
      throw new ConcurrentResourceModificationException("feedstock batch", null);
    }
  }
}
