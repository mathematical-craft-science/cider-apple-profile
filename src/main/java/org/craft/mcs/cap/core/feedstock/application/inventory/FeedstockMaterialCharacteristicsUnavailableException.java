package org.craft.mcs.cap.core.feedstock.application.inventory;

import java.io.Serial;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.ApplicationUnprocessableException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Indicates that current Feedstock evidence cannot describe a source for composition planning. */
@SuppressWarnings("java:S110")
public final class FeedstockMaterialCharacteristicsUnavailableException
    extends ApplicationUnprocessableException {

  @Serial
  private static final long serialVersionUID = 1L;

  public FeedstockMaterialCharacteristicsUnavailableException(
      @NonNull FeedstockBatchId batchId
  ) {
    super(
        "feedstock batch requires current Specific Gravity and Titratable Acidity for composition: "
            + requireNonNull(batchId, "feedstock batch id cannot be null").value()
    );
  }
}
