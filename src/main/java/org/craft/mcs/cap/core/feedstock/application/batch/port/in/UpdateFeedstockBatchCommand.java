package org.craft.mcs.cap.core.feedstock.application.batch.port.in;

import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockVessel;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record UpdateFeedstockBatchCommand(
    @NonNull FeedstockBatchId batchId,
    @NonNull WorkspaceId workspaceId,
    @Nullable FeedstockBatchName name,
    @Nullable FeedstockVessel vessel,
    boolean clearVessel,
    @Nullable FeedstockType type,
    @Nullable FeedstockSeason season,
    @Nullable FeedstockSource source,
    boolean clearSource,
    @Nullable VolumeValue availableVolume,
    @Nullable String note,
    boolean clearNote
) {

  public UpdateFeedstockBatchCommand {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
  }
}
