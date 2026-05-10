package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockSampleName;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.core.fermentation.domain.PotentialAbvPrediction;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockSamplePotentialAbvPrediction(
    @NonNull FeedstockSampleId sampleId,
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockBatchId batchId,
    @NonNull FeedstockSampleName sampleName,
    @NonNull MeasurementValueKind originalGravityValueKind,
    @NonNull PotentialAbvPrediction prediction
) {

  public FeedstockSamplePotentialAbvPrediction {
    requireNonNull(sampleId, "feedstock sample id cannot be null");
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(sampleName, "feedstock sample name cannot be null");
    requireNonNull(originalGravityValueKind, "original gravity value kind cannot be null");
    requireNonNull(prediction, "potential ABV prediction cannot be null");
  }
}
