package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.FeedstockSampleId;
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
}
