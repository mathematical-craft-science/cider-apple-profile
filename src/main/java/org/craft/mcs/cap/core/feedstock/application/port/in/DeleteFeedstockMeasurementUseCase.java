package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.jspecify.annotations.NonNull;


public interface DeleteFeedstockMeasurementUseCase {

  void delete(@NonNull DeleteFeedstockMeasurementCommand command);
}
