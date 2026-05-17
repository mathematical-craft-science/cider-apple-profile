package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock;

import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock.entity.FeedstockMeasurementJpaEntity;
import org.jspecify.annotations.NonNull;


record FeedstockMeasurementSummaryJpaProjection(
    @NonNull UUID ciderMakerId,
    @NonNull FeedstockMeasurementJpaEntity measurement
) {
}
