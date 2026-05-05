package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jpa.feedstock;

import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;


interface SpringDataFeedstockSampleJpaRepository
    extends JpaRepository<FeedstockSampleJpaEntity, @NonNull UUID> {
}
