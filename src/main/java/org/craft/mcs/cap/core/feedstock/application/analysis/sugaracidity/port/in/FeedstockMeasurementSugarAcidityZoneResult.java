package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Historical feedstock-measurement identity bound to a generic sugar-acidity profile evaluation.
 */
public record FeedstockMeasurementSugarAcidityZoneResult(
    @NonNull FeedstockMeasurementId measurementId,
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockMeasurementName measurementName,
    @NonNull SugarAcidityProfileEvaluation evaluation
) {

  public FeedstockMeasurementSugarAcidityZoneResult {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(measurementName, "feedstock measurement name cannot be null");
    requireNonNull(evaluation, "sugar-acidity profile evaluation cannot be null");
  }
}
