package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in;

import java.time.Instant;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Current feedstock-batch identity bound to a generic sugar-acidity profile evaluation.
 */
public record FeedstockBatchCurrentSugarAcidityZoneResult(
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockBatchName batchName,
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockMeasurementId currentMeasurementId,
    @NonNull Instant measuredAt,
    @NonNull SugarAcidityProfileEvaluation evaluation
) {

  public FeedstockBatchCurrentSugarAcidityZoneResult {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(batchName, "feedstock batch name cannot be null");
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(currentMeasurementId, "current feedstock measurement id cannot be null");
    requireNonNull(measuredAt, "feedstock measurement time cannot be null");
    requireNonNull(evaluation, "sugar-acidity profile evaluation cannot be null");
  }
}
