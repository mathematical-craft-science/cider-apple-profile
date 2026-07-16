package org.craft.mcs.cap.core.feedstock.application.measurement.port.in;

import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementSummary;
import org.jspecify.annotations.NonNull;


public interface UpdateFeedstockMeasurementUseCase {

  @NonNull
  FeedstockMeasurementSummary update(@NonNull UpdateFeedstockMeasurementCommand command);
}
