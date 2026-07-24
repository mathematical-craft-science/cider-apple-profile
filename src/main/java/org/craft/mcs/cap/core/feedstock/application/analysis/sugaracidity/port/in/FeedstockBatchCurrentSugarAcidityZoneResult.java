package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in;

import java.time.Instant;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Current feedstock-batch identity bound to a generic sugar-acidity profile evaluation.
 */
public record FeedstockBatchCurrentSugarAcidityZoneResult(
    @NonNull FeedstockBatchId batchId,
    @NonNull BatchName batchName,
    @NonNull WorkspaceId workspaceId,
    @NonNull FeedstockMeasurementId currentMeasurementId,
    @NonNull Instant measuredAt,
    @NonNull SugarAcidityProfileEvaluation evaluation
) {

  public FeedstockBatchCurrentSugarAcidityZoneResult {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(batchName, "feedstock batch name cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(currentMeasurementId, "current feedstock measurement id cannot be null");
    requireNonNull(measuredAt, "feedstock measurement time cannot be null");
    requireNonNull(evaluation, "sugar-acidity profile evaluation cannot be null");
  }
}
