package org.craft.mcs.cap.core.feedstock.application.measurement.port.out;

import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregateRepository;
import org.jspecify.annotations.NonNull;

public interface FeedstockMeasurementRepository
    extends VersionedAggregateRepository<FeedstockMeasurement, FeedstockMeasurementId> {

  void delete(@NonNull VersionedAggregate<FeedstockMeasurement> measurement);
}
