package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.jspecify.annotations.NonNull;


public interface PredictFeedstockMeasurementPotentialAbvUseCase {

  @NonNull
  FeedstockMeasurementPotentialAbvPrediction predict(@NonNull FeedstockMeasurementId measurementId);
}
