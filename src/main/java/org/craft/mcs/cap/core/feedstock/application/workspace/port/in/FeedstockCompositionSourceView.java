package org.craft.mcs.cap.core.feedstock.application.workspace.port.in;

import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockCompositionSourceView(
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockBatchName batchName,
    @Nullable VolumeValue volume
) {

  public FeedstockCompositionSourceView {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(batchName, "feedstock batch name cannot be null");
  }
}
