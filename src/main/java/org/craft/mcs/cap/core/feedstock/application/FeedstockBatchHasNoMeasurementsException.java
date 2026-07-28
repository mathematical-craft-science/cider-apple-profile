package org.craft.mcs.cap.core.feedstock.application;

import java.io.Serial;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.ApplicationUnprocessableException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@SuppressWarnings("java:S110")
public final class FeedstockBatchHasNoMeasurementsException
    extends ApplicationUnprocessableException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull FeedstockBatchId batchId;

  public FeedstockBatchHasNoMeasurementsException(@NonNull FeedstockBatchId batchId) {
    super(
        "feedstock batch has no measurements: "
            + requireNonNull(batchId, "feedstock batch id cannot be null").value()
    );
    this.batchId = batchId;
  }

  public @NonNull FeedstockBatchId batchId() {
    return batchId;
  }
}
