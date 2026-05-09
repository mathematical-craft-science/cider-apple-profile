package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock.entity.FeedstockSampleJpaMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


@Repository
@RequiredArgsConstructor
class JpaFeedstockSampleRepository
    implements FeedstockSampleRepository {

  private final @NonNull SpringDataFeedstockSampleJpaRepository repository;

  @Override
  public @NonNull FeedstockSample save(@NonNull FeedstockSample feedstockSample) {
    requireNonNull(feedstockSample, "feedstock sample cannot be null");

    final var entity = FeedstockSampleJpaMapper.fromDomain(feedstockSample);
    final var saved = repository.save(entity);

    return FeedstockSampleJpaMapper.toDomain(saved);
  }

  @Override
  public @NonNull Optional<@NonNull FeedstockSample> findById(@NonNull FeedstockSampleId sampleId) {
    requireNonNull(sampleId, "feedstock sample id cannot be null");

    return repository.findById(sampleId.value())
        .map(FeedstockSampleJpaMapper::toDomain);
  }

  @Override
  public @NonNull List<@NonNull FeedstockSample> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId) {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");

    return repository.findAllByCiderMakerId(ciderMakerId.value())
        .stream()
        .map(FeedstockSampleJpaMapper::toDomain)
        .toList();
  }
}
