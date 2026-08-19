package org.craft.mcs.cap.core.feedstock.batch.application;

import java.io.Serial;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.ApplicationConflictException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Indicates that initial vessel assignment can no longer be corrected after material operations. */
@SuppressWarnings("java:S110")
public final class FeedstockBatchVesselChangeNotAllowedException extends ApplicationConflictException {

  @Serial
  private static final long serialVersionUID = 1L;

  public FeedstockBatchVesselChangeNotAllowedException(@NonNull FeedstockBatchId batchId) {
    super("feedstock batch vessel cannot be changed after the first operation: "
        + requireNonNull(batchId, "feedstock batch id cannot be null").value());
  }
}
