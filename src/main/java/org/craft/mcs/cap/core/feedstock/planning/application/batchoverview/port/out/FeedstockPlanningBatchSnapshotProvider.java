package org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.port.out;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.model.FeedstockPlanningBatchSnapshot;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Supplies Planning Board batch snapshots through the Feedstock batch published boundary. */
public interface FeedstockPlanningBatchSnapshotProvider {

  @NonNull
  List<@NonNull FeedstockPlanningBatchSnapshot> findBatches(
      @NonNull WorkspaceId workspaceId,
      @Nullable FeedstockSeason season
  );
}
