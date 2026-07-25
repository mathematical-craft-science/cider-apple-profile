package org.craft.mcs.cap.core.feedstock.application.measurement.query;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.CurrentFeedstockBatchSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application query boundary for the current chemical state of a {@code FeedstockBatch}.
 *
 * <p>Feedstock measurements are historical evidence. Batch-level decisions use a state-aware
 * composite profile that selects the nearest applicable evidence for each characteristic along
 * the current material-state lineage.
 */
@Component
@RequiredArgsConstructor
public final class FeedstockBatchCurrentMeasurementQueryService {

  private final @NonNull FeedstockMeasurementQueryRepository feedstockMeasurementQueryRepository;

  public @NonNull FeedstockMeasurementView requireCurrentMeasurement(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");

    return findCurrentMeasurement(batchId)
        .orElseThrow(() -> new FeedstockBatchHasNoMeasurementsException(batchId));
  }

  public @NonNull Optional<@NonNull FeedstockMeasurementView> findCurrentMeasurement(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    return feedstockMeasurementQueryRepository.findCurrentByBatchId(batchId);
  }

  public @NonNull List<@NonNull FeedstockMeasurementView> findCurrentSugarAcidityPointCandidates(
      @NonNull CurrentFeedstockBatchSugarAcidityPointCandidateQuery query
  ) {
    requireNonNull(query, "current feedstock batch sugar-acidity point candidate query cannot be null");

    return feedstockMeasurementQueryRepository.findCurrentSugarAcidityPointCandidates(query);
  }

  public @NonNull List<@NonNull FeedstockMeasurementView> findCurrentMeasurementsByBatchIds(
      @NonNull Set<@NonNull FeedstockBatchId> batchIds
  ) {
    requireNonNull(batchIds, "feedstock batch ids cannot be null");
    if (batchIds.isEmpty()) {
      return List.of();
    }

    return feedstockMeasurementQueryRepository.findCurrentByBatchIds(batchIds);
  }
}
