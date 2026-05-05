package org.craft.mcs.cap.core.feedstock.application.port.out;

import org.craft.mcs.cap.core.feedstock.domain.model.FeedstockSample;
import org.jspecify.annotations.NonNull;


public interface FeedstockSampleRepository {

  @NonNull
  FeedstockSample save(@NonNull FeedstockSample feedstockSample);
}
