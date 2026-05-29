package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.measurement;

import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.measurement.entity.FeedstockMeasurementJdbcEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.ListCrudRepository;


interface SpringDataFeedstockMeasurementJdbcRepository
    extends ListCrudRepository<FeedstockMeasurementJdbcEntity, @NonNull UUID> {
}
