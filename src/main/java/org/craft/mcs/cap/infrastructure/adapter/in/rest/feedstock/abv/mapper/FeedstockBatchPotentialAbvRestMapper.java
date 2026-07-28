package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.abv.mapper;

import org.craft.mcs.cap.core.feedstock.application.analysis.abv.port.in.FeedstockBatchCurrentPotentialAbvPrediction;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.abv.dto.FeedstockBatchCurrentPotentialAbvResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class FeedstockBatchPotentialAbvRestMapper {

  private FeedstockBatchPotentialAbvRestMapper() {
  }

  public static @NonNull FeedstockBatchCurrentPotentialAbvResponse toResponse(
      @NonNull FeedstockBatchCurrentPotentialAbvPrediction prediction
  ) {
    requireNonNull(prediction, "feedstock batch current potential ABV prediction cannot be null");

    return new FeedstockBatchCurrentPotentialAbvResponse(
        prediction.batchId().value(),
        prediction.workspaceId().value(),
        prediction.currentMaterialStateId().value(),
        prediction.currentMeasurementId().value(),
        prediction.currentMeasurementName().value(),
        prediction.originalGravityValueKind(),
        PotentialAbvPredictionRestMapper.toResponse(prediction.prediction())
    );
  }
}
