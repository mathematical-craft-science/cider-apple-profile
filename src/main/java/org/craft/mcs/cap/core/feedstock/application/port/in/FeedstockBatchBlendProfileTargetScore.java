package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileTargetDistance;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.point.SugarAcidityProfilePoint;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FeedstockBatchBlendProfileTargetScore(
    @NonNull FeedstockBatchId batchId,
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockMeasurementId currentMeasurementId,
    @NonNull FeedstockMeasurementName currentMeasurementName,
    @NonNull SugarAcidityProfilePoint point,
    @NonNull BlendProfileTargetDistance distance
) {

  public FeedstockBatchBlendProfileTargetScore {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(currentMeasurementId, "current feedstock measurement id cannot be null");
    requireNonNull(currentMeasurementName, "current feedstock measurement name cannot be null");
    requireNonNull(point, "sugar-acidity profile point cannot be null");
    requireNonNull(distance, "blend profile target distance cannot be null");
  }
}
