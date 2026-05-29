package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.entity.FeedstockBatchJdbcMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.support.JdbcPersistenceExceptionTranslator.translateOptimisticLocking;


@Repository
@RequiredArgsConstructor
class JdbcFeedstockBatchRepository
    implements FeedstockBatchRepository {

  private final @NonNull SpringDataFeedstockBatchJdbcRepository repository;

  @Override
  public @NonNull FeedstockBatch save(@NonNull FeedstockBatch feedstockBatch) {
    requireNonNull(feedstockBatch, "feedstock batch cannot be null");

    final var entity = repository.findById(feedstockBatch.batchId().value())
        .map(existing -> FeedstockBatchJdbcMapper.updateFromDomain(feedstockBatch, existing))
        .orElseGet(() -> FeedstockBatchJdbcMapper.fromDomain(feedstockBatch));
    final var saved = translateOptimisticLocking("feedstock batch", () -> repository.save(entity));

    return FeedstockBatchJdbcMapper.toDomain(saved);
  }

  @Override
  public @NonNull Optional<@NonNull FeedstockBatch> findById(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    return repository.findById(batchId.value())
        .map(FeedstockBatchJdbcMapper::toDomain);
  }

  @Override
  public @NonNull List<@NonNull FeedstockBatch> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId) {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");

    return repository.findAllByCiderMakerIdAndDeletedAtIsNullOrderByCreatedAtAscBatchIdAsc(ciderMakerId.value())
        .stream()
        .map(FeedstockBatchJdbcMapper::toDomain)
        .toList();
  }

  @Override
  public void delete(@NonNull FeedstockBatch feedstockBatch) {
    requireNonNull(feedstockBatch, "feedstock batch cannot be null");

    translateOptimisticLocking(
        "feedstock batch",
        () -> repository.softDeleteById(feedstockBatch.batchId().value())
    );
  }
}
