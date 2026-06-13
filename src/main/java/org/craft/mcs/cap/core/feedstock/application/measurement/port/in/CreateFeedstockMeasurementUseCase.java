package org.craft.mcs.cap.core.feedstock.application.measurement.port.in;

import org.jspecify.annotations.NonNull;


public interface CreateFeedstockMeasurementUseCase {

  @NonNull
  FeedstockMeasurementCreated create(@NonNull CreateFeedstockMeasurementCommand command);
}
