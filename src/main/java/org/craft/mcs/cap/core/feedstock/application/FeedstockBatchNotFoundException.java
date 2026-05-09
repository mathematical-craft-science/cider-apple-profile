package org.craft.mcs.cap.core.feedstock.application;

import java.io.Serial;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class FeedstockBatchNotFoundException
    extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull FeedstockBatchId batchId;

  public FeedstockBatchNotFoundException(@NonNull FeedstockBatchId batchId) {
    super("feedstock batch not found: " + requireNonNull(batchId, "feedstock batch id cannot be null").value());
    this.batchId = batchId;
  }

  public @NonNull FeedstockBatchId batchId() {
    return batchId;
  }
}
