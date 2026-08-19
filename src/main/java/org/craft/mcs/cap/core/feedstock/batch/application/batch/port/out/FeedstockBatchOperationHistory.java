package org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out;

import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;


/** Feedstock-owned query boundary for post-creation material operations. */
public interface FeedstockBatchOperationHistory {

  boolean hasOperations(@NonNull FeedstockBatchId batchId);
}
