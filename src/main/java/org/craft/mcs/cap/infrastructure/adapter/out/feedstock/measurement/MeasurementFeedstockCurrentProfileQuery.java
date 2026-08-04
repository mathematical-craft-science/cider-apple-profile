package org.craft.mcs.cap.infrastructure.adapter.out.feedstock.measurement;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.out.FeedstockCurrentProfileQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.out.FeedstockCurrentProfileSubjectQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Resolves Feedstock current profiles through the Measurement anti-corruption projector. */
@Component
@RequiredArgsConstructor
final class MeasurementFeedstockCurrentProfileQuery implements FeedstockCurrentProfileQueryRepository {

  private final @NonNull FeedstockCurrentProfileSubjectQuery subjectQuery;
  private final @NonNull MeasurementFeedstockCurrentProfileProjector projector;

  @Override
  public @NonNull Optional<@NonNull FeedstockCurrentProfileView> findByBatchId(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    return projector.project(subjectQuery.findByBatchIds(Set.of(batchId))).stream().findFirst();
  }

  @Override
  public @NonNull List<@NonNull FeedstockCurrentProfileView> findByBatchIds(
      @NonNull Set<@NonNull FeedstockBatchId> batchIds
  ) {
    requireNonNull(batchIds, "feedstock batch ids cannot be null");
    return projector.project(subjectQuery.findByBatchIds(batchIds));
  }
}
