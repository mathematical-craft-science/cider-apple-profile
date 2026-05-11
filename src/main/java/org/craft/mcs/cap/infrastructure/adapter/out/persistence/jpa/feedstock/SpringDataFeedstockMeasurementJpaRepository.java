package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock;

import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock.entity.FeedstockMeasurementJpaEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;


interface SpringDataFeedstockMeasurementJpaRepository
    extends JpaRepository<FeedstockMeasurementJpaEntity, @NonNull UUID> {
}
