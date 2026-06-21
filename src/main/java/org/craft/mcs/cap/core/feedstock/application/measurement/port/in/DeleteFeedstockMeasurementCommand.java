package org.craft.mcs.cap.core.feedstock.application.measurement.port.in;

import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record DeleteFeedstockMeasurementCommand(
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockMeasurementId measurementId
) {

  public DeleteFeedstockMeasurementCommand {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
  }
}
