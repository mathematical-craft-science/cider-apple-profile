package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.jspecify.annotations.NonNull;


public interface CreateFeedstockMeasurementUseCase {

  @NonNull
  FeedstockMeasurementCreated create(@NonNull CreateFeedstockMeasurementCommand command);
}
