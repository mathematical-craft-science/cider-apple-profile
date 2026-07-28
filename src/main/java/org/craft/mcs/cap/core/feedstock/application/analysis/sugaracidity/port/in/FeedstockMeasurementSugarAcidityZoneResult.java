package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in;

import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementName;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessment;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Historical feedstock-measurement identity bound to a generic sugar-acidity profile evaluation.
 */
public record FeedstockMeasurementSugarAcidityZoneResult(
    @NonNull FeedstockMeasurementId measurementId,
    @NonNull WorkspaceId workspaceId,
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockMeasurementName measurementName,
    @NonNull ChemicalProfileAssessment evaluation
) {

  public FeedstockMeasurementSugarAcidityZoneResult {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(measurementName, "feedstock measurement name cannot be null");
    requireNonNull(evaluation, "sugar-acidity profile evaluation cannot be null");
  }
}
