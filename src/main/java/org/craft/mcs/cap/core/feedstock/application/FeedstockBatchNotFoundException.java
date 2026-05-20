package org.craft.mcs.cap.core.feedstock.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@SuppressWarnings("java:S110")
public final class FeedstockBatchNotFoundException
    extends ApplicationNotFoundException {

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
