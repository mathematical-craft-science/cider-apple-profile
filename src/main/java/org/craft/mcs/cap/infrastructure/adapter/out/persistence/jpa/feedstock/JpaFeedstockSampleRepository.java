package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock;

import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.FeedstockSample;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


@Repository
final class JpaFeedstockSampleRepository
    implements FeedstockSampleRepository {

  private final @NonNull SpringDataFeedstockSampleJpaRepository repository;

  JpaFeedstockSampleRepository(@NonNull SpringDataFeedstockSampleJpaRepository repository) {
    this.repository = requireNonNull(repository, "spring data feedstock sample repository cannot be null");
  }

  @Override
  public @NonNull FeedstockSample save(@NonNull FeedstockSample feedstockSample) {
    requireNonNull(feedstockSample, "feedstock sample cannot be null");

    final var entity = FeedstockSampleJpaMapper.fromDomain(feedstockSample);
    final var saved = repository.save(entity);

    return FeedstockSampleJpaMapper.toDomain(saved);
  }
}
