package org.craft.mcs.cap.core.feedstock.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationUnprocessableException;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Indicates that a feedstock batch cannot provide the requested material volume. */
@SuppressWarnings("java:S110")
public final class FeedstockBatchInsufficientAvailableVolumeException
    extends ApplicationUnprocessableException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull FeedstockBatchId batchId;
  private final transient @NonNull VolumeValue requestedVolume;
  private final transient @NonNull VolumeValue availableVolume;

  public FeedstockBatchInsufficientAvailableVolumeException(
      @NonNull FeedstockBatchId batchId,
      @NonNull VolumeValue requestedVolume,
      @NonNull VolumeValue availableVolume
  ) {
    super("not enough available volume in feedstock batch: "
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
