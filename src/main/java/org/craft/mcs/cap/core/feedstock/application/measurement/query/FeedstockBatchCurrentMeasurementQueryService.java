package org.craft.mcs.cap.core.feedstock.application.measurement.query;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.LatestFeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application query boundary for the current chemical state of a {@code FeedstockBatch}.
 *
 * <p>Feedstock measurements are historical observations. Batch-level decisions must use only the
 * current measurement, defined as the measurement with the greatest {@code measuredAt}; ties are
 * resolved by the greatest measurement id. Persistence adapters may optimize the query, but use
 * cases should depend on this service so the rule remains explicit in the application layer.
 */
@Component
@RequiredArgsConstructor
public final class FeedstockBatchCurrentMeasurementQueryService {

  private final @NonNull FeedstockMeasurementQueryRepository feedstockMeasurementQueryRepository;

  public @NonNull FeedstockMeasurement requireCurrentMeasurement(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    return feedstockMeasurementQueryRepository.findLatestByBatchId(batchId)
        .orElseThrow(() -> new FeedstockBatchHasNoMeasurementsException(batchId));
  }

  public @NonNull List<@NonNull FeedstockMeasurement> findCurrentSugarAcidityPointCandidates(
      @NonNull LatestFeedstockMeasurementSugarAcidityPointCandidateQuery query
  ) {
    requireNonNull(query, "current feedstock batch sugar-acidity point candidate query cannot be null");

    return feedstockMeasurementQueryRepository.findLatestSugarAcidityPointCandidates(query);
  }

  public @NonNull List<@NonNull FeedstockMeasurement> findCurrentMeasurementsByBatchIds(
      @NonNull Set<@NonNull FeedstockBatchId> batchIds
  ) {
    requireNonNull(batchIds, "feedstock batch ids cannot be null");
    if (batchIds.isEmpty()) {
      return List.of();
    }

    return feedstockMeasurementQueryRepository.findLatestByBatchIds(batchIds);
  }
}
