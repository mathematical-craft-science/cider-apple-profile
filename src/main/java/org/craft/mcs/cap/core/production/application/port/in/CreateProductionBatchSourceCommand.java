package org.craft.mcs.cap.core.production.application.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record CreateProductionBatchSourceCommand(
    @NonNull FeedstockBatchId feedstockBatchId,
    @NonNull VolumeValue volume
) {

  public CreateProductionBatchSourceCommand {
    requireNonNull(feedstockBatchId, "feedstock batch id cannot be null");
    requireNonNull(volume, "production source volume cannot be null");
    if (volume.isZero()) {
      throw new IllegalArgumentException("production source volume must be positive");
    }
  }
}
