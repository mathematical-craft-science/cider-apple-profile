package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock;

import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


record FeedstockSampleSummaryJpaProjection(
    @NonNull UUID sampleId,
    @NonNull UUID ciderMakerId,
    @NonNull UUID batchId,
    @NonNull String name,
    @NonNull FeedstockType type,
    @NonNull Instant measuredAt,
    @Nullable String note
) {
}
