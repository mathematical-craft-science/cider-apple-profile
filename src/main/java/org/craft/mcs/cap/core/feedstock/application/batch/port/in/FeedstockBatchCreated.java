package org.craft.mcs.cap.core.feedstock.application.batch.port.in;

import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchView;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockBatchCreated(
    @NonNull FeedstockBatchView batch
) {

  public FeedstockBatchCreated {
    requireNonNull(batch, "created feedstock batch cannot be null");
  }
}
