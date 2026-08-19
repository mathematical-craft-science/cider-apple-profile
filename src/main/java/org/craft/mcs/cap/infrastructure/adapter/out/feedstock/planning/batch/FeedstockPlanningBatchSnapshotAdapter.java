package org.craft.mcs.cap.infrastructure.adapter.out.feedstock.planning.batch;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchView;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.ListFeedstockBatchesQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.ListFeedstockBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.in.AssessFeedstockCurrentProfileUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.in.QueryFeedstockCurrentProfilesUseCase;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.model.FeedstockPlanningBatchSnapshot;
import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.port.out.FeedstockPlanningBatchSnapshotProvider;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** ACL composing published Feedstock batch views into Planning Board snapshots. */
@Component
@RequiredArgsConstructor
public final class FeedstockPlanningBatchSnapshotAdapter
    implements FeedstockPlanningBatchSnapshotProvider {

  private final @NonNull ListFeedstockBatchesUseCase listFeedstockBatchesUseCase;
  private final @NonNull QueryFeedstockCurrentProfilesUseCase queryCurrentProfilesUseCase;
  private final @NonNull AssessFeedstockCurrentProfileUseCase assessCurrentProfileUseCase;

  @Override
  public @NonNull List<@NonNull FeedstockPlanningBatchSnapshot> findBatches(
      @NonNull WorkspaceId workspaceId,
      @Nullable FeedstockSeason season
  ) {
    final var batches = listFeedstockBatchesUseCase.list(new ListFeedstockBatchesQuery(workspaceId, season));
    final var profilesByBatchId = queryCurrentProfilesUseCase.findCurrentProfiles(
            batches.stream().map(FeedstockBatchView::batch).toList()
        ).stream()
        .collect(Collectors.toUnmodifiableMap(
            FeedstockCurrentProfileView::batchId,
            Function.identity()
        ));

    return batches.stream()
        .map(batch -> snapshot(batch, profilesByBatchId.get(batch.batch().batchId())))
        .toList();
  }

  private @NonNull FeedstockPlanningBatchSnapshot snapshot(
      @NonNull FeedstockBatchView batch,
      @Nullable FeedstockCurrentProfileView currentProfile
  ) {
    return new FeedstockPlanningBatchSnapshot(
        batch,
        currentProfile,
        assessCurrentProfileUseCase.assess(batch.batch(), currentProfile)
    );
  }
}
