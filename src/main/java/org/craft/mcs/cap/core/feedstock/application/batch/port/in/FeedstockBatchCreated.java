package org.craft.mcs.cap.core.feedstock.application.batch.port.in;

import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockBatchCreated(
    @NonNull FeedstockBatchId batchId,
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockBatchName name,
    @NonNull FeedstockType type,
    @NonNull FeedstockSeason season,
    @Nullable FeedstockSource source,
    @NonNull VolumeValue availableVolume,
    @Nullable String note
) {

  public FeedstockBatchCreated {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(name, "feedstock batch name cannot be null");
    requireNonNull(type, "feedstock type cannot be null");
    requireNonNull(season, "feedstock season cannot be null");
    requireNonNull(availableVolume, "available volume cannot be null");
  }
}
