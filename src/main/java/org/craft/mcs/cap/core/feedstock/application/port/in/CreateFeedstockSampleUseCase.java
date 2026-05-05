package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.FeedstockSample;
import org.jspecify.annotations.NonNull;


public interface CreateFeedstockSampleUseCase {

  @NonNull
  FeedstockSample create(@NonNull CreateFeedstockSampleCommand command);
}
