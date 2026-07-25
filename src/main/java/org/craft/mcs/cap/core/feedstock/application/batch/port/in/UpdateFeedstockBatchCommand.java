package org.craft.mcs.cap.core.feedstock.application.batch.port.in;

import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record UpdateFeedstockBatchCommand(
    @NonNull FeedstockBatchId batchId,
    @NonNull WorkspaceId workspaceId,
    @Nullable BatchName name,
    @Nullable FeedstockType type,
    @Nullable FeedstockSeason season,
    @Nullable FeedstockSource source,
    boolean clearSource,
    @Nullable String note,
    boolean clearNote
) {

  public UpdateFeedstockBatchCommand {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
  }
}
