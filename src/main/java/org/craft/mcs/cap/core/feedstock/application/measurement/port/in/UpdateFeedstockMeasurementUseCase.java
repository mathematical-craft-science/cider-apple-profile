package org.craft.mcs.cap.core.feedstock.application.measurement.port.in;

import org.jspecify.annotations.NonNull;


public interface UpdateFeedstockMeasurementUseCase {

  @NonNull
  FeedstockMeasurementSummary update(@NonNull UpdateFeedstockMeasurementCommand command);
}
