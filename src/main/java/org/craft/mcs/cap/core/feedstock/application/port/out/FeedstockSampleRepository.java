package org.craft.mcs.cap.core.feedstock.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.domain.model.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.FeedstockSampleId;
import org.jspecify.annotations.NonNull;


public interface FeedstockSampleRepository {

  @NonNull
  FeedstockSample save(@NonNull FeedstockSample feedstockSample);

  @NonNull
  Optional<@NonNull FeedstockSample> findById(@NonNull FeedstockSampleId sampleId);
}
