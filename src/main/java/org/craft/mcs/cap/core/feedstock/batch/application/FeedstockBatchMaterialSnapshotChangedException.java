package org.craft.mcs.cap.core.feedstock.batch.application;

import java.io.Serial;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.ApplicationConflictException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Raised when feedstock no longer matches a previously inspected material snapshot. */
@SuppressWarnings("java:S110")
public final class FeedstockBatchMaterialSnapshotChangedException extends ApplicationConflictException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull FeedstockBatchId batchId;

  public FeedstockBatchMaterialSnapshotChangedException(@NonNull FeedstockBatchId batchId) {
    super("feedstock batch material snapshot changed");
    this.batchId = requireNonNull(batchId, "feedstock batch id cannot be null");
  }

  public @NonNull FeedstockBatchId batchId() {
    return batchId;
  }
}
