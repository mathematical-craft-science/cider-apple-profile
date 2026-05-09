package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock.entity.FeedstockBatchJpaMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


@Repository
@RequiredArgsConstructor
class JpaFeedstockBatchRepository
    implements FeedstockBatchRepository {

  private final @NonNull SpringDataFeedstockBatchJpaRepository repository;

  @Override
  public @NonNull FeedstockBatch save(@NonNull FeedstockBatch feedstockBatch) {
    requireNonNull(feedstockBatch, "feedstock batch cannot be null");

    final var entity = FeedstockBatchJpaMapper.fromDomain(feedstockBatch);
    final var saved = repository.save(entity);

    return FeedstockBatchJpaMapper.toDomain(saved);
  }

  @Override
  public @NonNull Optional<@NonNull FeedstockBatch> findById(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    return repository.findById(batchId.value())
        .map(FeedstockBatchJpaMapper::toDomain);
  }

  @Override
  public @NonNull List<@NonNull FeedstockBatch> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId) {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");

    return repository.findAllByCiderMakerId(ciderMakerId.value())
        .stream()
        .map(FeedstockBatchJpaMapper::toDomain)
        .toList();
  }
}
