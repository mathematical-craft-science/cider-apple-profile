package org.craft.mcs.cap.core.feedstock.application.batch.usecase;

import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Converts the feedstock aggregate into the application summary used by read-side use cases.
 */
public final class FeedstockBatchSummaryMapper {

  private FeedstockBatchSummaryMapper() {
  }

  public static @NonNull FeedstockBatchSummary toSummary(@NonNull FeedstockBatch batch) {
    requireNonNull(batch, "feedstock batch cannot be null");
    return new FeedstockBatchSummary(
        batch.batchId(),
        batch.workspaceId(),
        batch.name(),
        batch.vessel(),
        batch.type(),
        batch.season(),
        batch.source(),
        batch.availableVolume(),
        batch.note()
    );
  }
}
