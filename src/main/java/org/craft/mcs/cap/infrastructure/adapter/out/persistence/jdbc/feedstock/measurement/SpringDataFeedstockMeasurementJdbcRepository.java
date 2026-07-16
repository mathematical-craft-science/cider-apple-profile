package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.measurement;

import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.measurement.entity.FeedstockMeasurementJdbcEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;


interface SpringDataFeedstockMeasurementJdbcRepository
    extends ListCrudRepository<FeedstockMeasurementJdbcEntity, @NonNull UUID> {

  @Modifying
  @Query("DELETE FROM feedstock_measurements WHERE measurement_id = :measurementId AND version = :version")
  int deleteByIdAndVersion(
      @Param("measurementId") @NonNull UUID measurementId,
      @Param("version") long version
  );
}
