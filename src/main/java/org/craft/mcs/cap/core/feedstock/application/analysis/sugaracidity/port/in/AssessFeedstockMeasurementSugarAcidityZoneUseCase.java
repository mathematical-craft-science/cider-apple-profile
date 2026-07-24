package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in;

import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.jspecify.annotations.NonNull;


public interface AssessFeedstockMeasurementSugarAcidityZoneUseCase {

  @NonNull
  FeedstockMeasurementSugarAcidityZoneResult assess(@NonNull FeedstockMeasurementId measurementId);
}
