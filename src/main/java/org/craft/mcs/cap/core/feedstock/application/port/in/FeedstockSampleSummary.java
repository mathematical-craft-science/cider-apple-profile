package org.craft.mcs.cap.core.feedstock.application.port.in;

import java.time.Instant;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockSampleName;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockSampleSummary(
    @NonNull FeedstockSampleId sampleId,
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockSampleName name,
    @NonNull FeedstockType type,
    @NonNull Instant measuredAt,
    @Nullable String note
) {

  public FeedstockSampleSummary {
    requireNonNull(sampleId, "feedstock sample id cannot be null");
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(name, "feedstock sample name cannot be null");
    requireNonNull(type, "feedstock type cannot be null");
    requireNonNull(measuredAt, "measured at cannot be null");
  }
}
