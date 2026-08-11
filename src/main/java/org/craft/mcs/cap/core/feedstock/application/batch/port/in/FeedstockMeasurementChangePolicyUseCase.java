package org.craft.mcs.cap.core.feedstock.application.batch.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;


/** Feedstock boundary that validates whether measurement evidence may be changed. */
public interface FeedstockMeasurementChangePolicyUseCase {

  void requireChangesAllowed(@NonNull FeedstockBatchId batchId);
}
