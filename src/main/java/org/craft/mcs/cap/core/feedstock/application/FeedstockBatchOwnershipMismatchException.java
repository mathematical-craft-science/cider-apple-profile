package org.craft.mcs.cap.core.feedstock.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationForbiddenException;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@SuppressWarnings("java:S110")
public final class FeedstockBatchOwnershipMismatchException
    extends ApplicationForbiddenException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull FeedstockBatchId batchId;
  private final transient @NonNull CiderMakerId expectedCiderMakerId;
  private final transient @NonNull CiderMakerId actualCiderMakerId;

  public FeedstockBatchOwnershipMismatchException(
      @NonNull FeedstockBatchId batchId,
      @NonNull CiderMakerId expectedCiderMakerId,
      @NonNull CiderMakerId actualCiderMakerId
  ) {
    super("feedstock batch belongs to another cider maker: "
        + requireNonNull(batchId, "feedstock batch id cannot be null").value());
    this.batchId = batchId;
    this.expectedCiderMakerId = requireNonNull(expectedCiderMakerId, "expected cider maker id cannot be null");
    this.actualCiderMakerId = requireNonNull(actualCiderMakerId, "actual cider maker id cannot be null");
  }

  public @NonNull FeedstockBatchId batchId() {
    return batchId;
  }

  public @NonNull CiderMakerId expectedCiderMakerId() {
    return expectedCiderMakerId;
  }

  public @NonNull CiderMakerId actualCiderMakerId() {
    return actualCiderMakerId;
  }
}
