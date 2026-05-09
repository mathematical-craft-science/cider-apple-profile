package org.craft.mcs.cap.core.feedstock.application;

import java.io.Serial;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class FeedstockBatchOwnershipMismatchException
    extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull FeedstockBatchId batchId;
  private final transient @NonNull CiderMakerId requestedCiderMakerId;
  private final transient @NonNull CiderMakerId actualCiderMakerId;

  public FeedstockBatchOwnershipMismatchException(
      @NonNull FeedstockBatchId batchId,
      @NonNull CiderMakerId requestedCiderMakerId,
      @NonNull CiderMakerId actualCiderMakerId
  ) {
    super("feedstock batch " + requireNonNull(batchId, "feedstock batch id cannot be null").value()
        + " belongs to cider maker "
        + requireNonNull(actualCiderMakerId, "actual cider maker id cannot be null").value()
        + ", not "
        + requireNonNull(requestedCiderMakerId, "requested cider maker id cannot be null").value());
    this.batchId = batchId;
    this.requestedCiderMakerId = requestedCiderMakerId;
    this.actualCiderMakerId = actualCiderMakerId;
  }

  public @NonNull FeedstockBatchId batchId() {
    return batchId;
  }

  public @NonNull CiderMakerId requestedCiderMakerId() {
    return requestedCiderMakerId;
  }

  public @NonNull CiderMakerId actualCiderMakerId() {
    return actualCiderMakerId;
  }
}
