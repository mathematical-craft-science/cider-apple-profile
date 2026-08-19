package org.craft.mcs.cap.infrastructure.adapter.out.feedstock.planning.batch;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePagePosition;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePageQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.in.QueryFeedstockCurrentProfilePageUseCase;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.model.FeedstockPlanningCandidatePagePosition;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.model.FeedstockPlanningCandidateQuery;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.model.FeedstockPlanningCandidateSnapshot;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.port.out.FeedstockPlanningCandidateProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/** ACL mapping Feedstock batch current-profile pages into planning candidate snapshots. */
@Component
@RequiredArgsConstructor
public final class FeedstockPlanningCandidateAdapter implements FeedstockPlanningCandidateProvider {

  private final @NonNull QueryFeedstockCurrentProfilePageUseCase queryCurrentProfilePageUseCase;

  @Override
  public @NonNull List<@NonNull FeedstockPlanningCandidateSnapshot> findCandidates(
      @NonNull FeedstockPlanningCandidateQuery query
  ) {
    requireNonNull(query, "feedstock planning candidate query cannot be null");
    return queryCurrentProfilePageUseCase.query(new FeedstockCurrentProfilePageQuery(
        query.workspaceId(),
        query.batch() == null ? null : new FeedstockBatchCriteria(query.batch().batchIds()),
        query.season(),
        toBatchPosition(query.after()),
        query.limit()
    )).stream()
        .map(item -> new FeedstockPlanningCandidateSnapshot(
            item.currentProfile(),
            item.batch().availableVolume()
        ))
        .toList();
  }

  private static @Nullable FeedstockCurrentProfilePagePosition toBatchPosition(
      @Nullable FeedstockPlanningCandidatePagePosition position
  ) {
    return position == null
        ? null
        : new FeedstockCurrentProfilePagePosition(position.measuredAt(), position.measurementSetId());
  }
}
