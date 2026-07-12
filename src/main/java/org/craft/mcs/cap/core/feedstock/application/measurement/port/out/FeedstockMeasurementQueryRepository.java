package org.craft.mcs.cap.core.feedstock.application.measurement.port.out;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.FeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.LatestFeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.jspecify.annotations.NonNull;


/**
 * Read-side access to feedstock measurements.
 *
 * <p>Analysis methods intentionally return {@link FeedstockMeasurement}: profile projection,
 * scoring and readiness policies need the semantic {@code ChemicalProfile} from the measurement.
 * Plain measurement-history reads use {@link FeedstockMeasurementSummary} instead.
 */
public interface FeedstockMeasurementQueryRepository {

  @NonNull
  List<@NonNull FeedstockMeasurement> findSugarAcidityPointCandidates(
      @NonNull FeedstockMeasurementSugarAcidityPointCandidateQuery query
  );

  @NonNull
  List<@NonNull FeedstockMeasurement> findLatestSugarAcidityPointCandidates(
      @NonNull LatestFeedstockMeasurementSugarAcidityPointCandidateQuery query
  );

  @NonNull
  Optional<@NonNull FeedstockMeasurement> findLatestByBatchId(@NonNull FeedstockBatchId batchId);

  @NonNull
  List<@NonNull FeedstockMeasurement> findLatestByBatchIds(@NonNull Set<@NonNull FeedstockBatchId> batchIds);

  @NonNull
  List<@NonNull FeedstockMeasurementSummary> findSummariesByBatchId(@NonNull FeedstockBatchId batchId);
}
