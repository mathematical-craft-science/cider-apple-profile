package org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out;

import java.time.Instant;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatch;
import org.jspecify.annotations.NonNull;


/** Records the initial immutable material state of a newly created feedstock batch. */
public interface FeedstockBatchTraceRecorder {

  void recordCreated(
      @NonNull FeedstockBatch batch,
      @NonNull Instant occurredAt
  );
}
