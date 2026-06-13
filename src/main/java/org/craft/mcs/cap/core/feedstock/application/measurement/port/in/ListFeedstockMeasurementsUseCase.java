package org.craft.mcs.cap.core.feedstock.application.measurement.port.in;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;


public interface ListFeedstockMeasurementsUseCase {

  @NonNull
  List<@NonNull FeedstockMeasurementSummary> listByBatchId(@NonNull FeedstockBatchId batchId);
}
