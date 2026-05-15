package org.craft.mcs.cap.core.production.application;

import java.io.Serial;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class ProductionSourceInsufficientAvailableVolumeException
    extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull FeedstockBatchId batchId;
  private final transient @NonNull VolumeValue requestedVolume;
  private final transient @NonNull VolumeValue availableVolume;

  public ProductionSourceInsufficientAvailableVolumeException(
      @NonNull FeedstockBatchId batchId,
      @NonNull VolumeValue requestedVolume,
      @NonNull VolumeValue availableVolume
  ) {
    super("not enough feedstock volume in batch: "
        + requireNonNull(batchId, "feedstock batch id cannot be null").value());
    this.batchId = batchId;
    this.requestedVolume = requireNonNull(requestedVolume, "requested volume cannot be null");
    this.availableVolume = requireNonNull(availableVolume, "available volume cannot be null");
  }

  public @NonNull FeedstockBatchId batchId() {
    return batchId;
  }

  public @NonNull VolumeValue requestedVolume() {
    return requestedVolume;
  }

  public @NonNull VolumeValue availableVolume() {
    return availableVolume;
  }
}
