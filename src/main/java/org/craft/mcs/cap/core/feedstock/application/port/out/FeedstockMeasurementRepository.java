package org.craft.mcs.cap.core.feedstock.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.jspecify.annotations.NonNull;


public interface FeedstockMeasurementRepository {

  @NonNull
  FeedstockMeasurement save(@NonNull FeedstockMeasurement feedstockMeasurement);

  @NonNull
  Optional<@NonNull FeedstockMeasurement> findById(@NonNull FeedstockMeasurementId measurementId);
}
