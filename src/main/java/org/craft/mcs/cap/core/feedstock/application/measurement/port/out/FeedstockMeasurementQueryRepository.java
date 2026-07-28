package org.craft.mcs.cap.core.feedstock.application.measurement.port.out;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.jspecify.annotations.NonNull;


public interface FeedstockMeasurementQueryRepository {

  @NonNull
  Optional<@NonNull FeedstockMeasurementView> findById(@NonNull FeedstockMeasurementId measurementId);

  @NonNull
  List<@NonNull FeedstockMeasurementSummary> findSummariesByBatchId(@NonNull FeedstockBatchId batchId);
}
