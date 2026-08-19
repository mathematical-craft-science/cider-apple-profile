package org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in;

import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchView;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockBatchCreated(
    @NonNull FeedstockBatchView batch
) {

  public FeedstockBatchCreated {
    requireNonNull(batch, "created feedstock batch cannot be null");
  }
}
