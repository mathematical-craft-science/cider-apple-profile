package org.craft.mcs.cap.core.feedstock.application.analysis.abv.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.PotentialAbvPrediction;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockMeasurementPotentialAbvPrediction(
    @NonNull FeedstockMeasurementId measurementId,
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockMeasurementName measurementName,
    @NonNull MeasurementValueKind originalGravityValueKind,
    @NonNull PotentialAbvPrediction prediction
) {

  public FeedstockMeasurementPotentialAbvPrediction {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(measurementName, "feedstock measurement name cannot be null");
    requireNonNull(originalGravityValueKind, "original gravity value kind cannot be null");
    requireNonNull(prediction, "potential ABV prediction cannot be null");
  }
}
