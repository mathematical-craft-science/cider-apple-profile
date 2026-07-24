package org.craft.mcs.cap.core.feedstock.application.measurement.port.out;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.FeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.LatestFeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.jspecify.annotations.NonNull;


public interface FeedstockMeasurementQueryRepository {

  @NonNull
  Optional<@NonNull FeedstockMeasurementView> findById(@NonNull FeedstockMeasurementId measurementId);

  @NonNull
  List<@NonNull FeedstockMeasurementView> findSugarAcidityPointCandidates(
      @NonNull FeedstockMeasurementSugarAcidityPointCandidateQuery query
  );

  @NonNull
  List<@NonNull FeedstockMeasurementView> findLatestSugarAcidityPointCandidates(
      @NonNull LatestFeedstockMeasurementSugarAcidityPointCandidateQuery query
  );

  @NonNull
  Optional<@NonNull FeedstockMeasurementView> findLatestByBatchId(@NonNull FeedstockBatchId batchId);

  @NonNull
  List<@NonNull FeedstockMeasurementView> findLatestByBatchIds(@NonNull Set<@NonNull FeedstockBatchId> batchIds);

  @NonNull
  List<@NonNull FeedstockMeasurementSummary> findSummariesByBatchId(@NonNull FeedstockBatchId batchId);
}
