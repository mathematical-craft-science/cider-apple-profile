package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.jspecify.annotations.NonNull;


public interface AssessFeedstockMeasurementSugarAcidityZoneUseCase {

  @NonNull
  FeedstockMeasurementSugarAcidityZoneResult assess(@NonNull FeedstockMeasurementId measurementId);
}
