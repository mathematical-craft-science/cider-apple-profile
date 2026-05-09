package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.jspecify.annotations.NonNull;


public interface PredictFeedstockSamplePotentialAbvUseCase {

  @NonNull
  FeedstockSamplePotentialAbvPrediction predict(@NonNull FeedstockSampleId sampleId);
}
