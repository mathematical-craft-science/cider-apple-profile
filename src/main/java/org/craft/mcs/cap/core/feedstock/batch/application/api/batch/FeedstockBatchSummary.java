package org.craft.mcs.cap.core.feedstock.batch.application.api.batch;

import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockBatchSummary(
    @NonNull FeedstockBatchId batchId,
    @NonNull WorkspaceId workspaceId,
    @NonNull BatchName name,
    @NonNull VesselId vesselId,
    @NonNull FeedstockType type,
    @NonNull FeedstockSeason season,
    @Nullable FeedstockSource source,
    @NonNull BatchLifecycle lifecycle,
    @NonNull VolumeValue availableVolume,
    @NonNull MaterialStateId currentMaterialStateId,
    @Nullable String note
) {

  public FeedstockBatchSummary {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(name, "feedstock batch name cannot be null");
    requireNonNull(vesselId, "feedstock vessel id cannot be null");
    requireNonNull(type, "feedstock type cannot be null");
    requireNonNull(season, "feedstock season cannot be null");
    requireNonNull(lifecycle, "feedstock batch lifecycle cannot be null");
    requireNonNull(availableVolume, "available volume cannot be null");
    requireNonNull(currentMaterialStateId, "feedstock current material state id cannot be null");
  }
}
