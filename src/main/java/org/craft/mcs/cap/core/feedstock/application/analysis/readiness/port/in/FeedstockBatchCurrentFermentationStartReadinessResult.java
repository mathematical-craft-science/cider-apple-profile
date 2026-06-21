package org.craft.mcs.cap.core.feedstock.application.analysis.readiness.port.in;

import org.craft.mcs.cap.core.cideranalysis.fermentationstart.domain.FermentationStartReadinessAssessment;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockBatchCurrentFermentationStartReadinessResult(
    @NonNull FeedstockBatchId batchId,
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockMeasurementId currentMeasurementId,
    @NonNull FeedstockMeasurementName currentMeasurementName,
    @NonNull FermentationStartReadinessAssessment assessment
) {

  public FeedstockBatchCurrentFermentationStartReadinessResult {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(currentMeasurementId, "current measurement id cannot be null");
    requireNonNull(currentMeasurementName, "current measurement name cannot be null");
    requireNonNull(assessment, "fermentation start readiness assessment cannot be null");
  }
}
