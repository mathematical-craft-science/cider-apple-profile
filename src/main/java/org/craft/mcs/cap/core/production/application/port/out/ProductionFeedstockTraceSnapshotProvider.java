package org.craft.mcs.cap.core.production.application.port.out;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;


/**
 * Outbound port for obtaining the current trace snapshot of a feedstock source.
 *
 * <p>Production owns the creation decision, but the definition of a current feedstock profile
 * belongs to feedstock. The adapter behind this port applies that feedstock rule and returns a
 * stable snapshot for traceability.
 */
public interface ProductionFeedstockTraceSnapshotProvider {

  @NonNull
  ProductionFeedstockTraceSnapshot currentSnapshotFor(@NonNull FeedstockBatchId batchId);
}
