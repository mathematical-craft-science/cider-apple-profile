package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


record FeedstockMeasurementSummaryJpaProjection(
    @NonNull UUID measurementId,
    @NonNull UUID ciderMakerId,
    @NonNull UUID batchId,
    @NonNull String name,
    @NonNull Instant measuredAt,
    @Nullable String note
) {
}
