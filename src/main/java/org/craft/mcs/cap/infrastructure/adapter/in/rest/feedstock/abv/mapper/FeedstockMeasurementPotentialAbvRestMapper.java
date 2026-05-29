package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.abv.mapper;

import org.craft.mcs.cap.core.feedstock.application.analysis.abv.port.in.FeedstockMeasurementPotentialAbvPrediction;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.abv.dto.FeedstockMeasurementPotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class FeedstockMeasurementPotentialAbvRestMapper {

  private FeedstockMeasurementPotentialAbvRestMapper() {
  }

  public static @NonNull FeedstockMeasurementPotentialAbvResponse toResponse(
      @NonNull FeedstockMeasurementPotentialAbvPrediction prediction
  ) {
    requireNonNull(prediction, "feedstock measurement potential ABV prediction cannot be null");

    return new FeedstockMeasurementPotentialAbvResponse(
        prediction.measurementId().value(),
        prediction.ciderMakerId().value(),
        prediction.batchId().value(),
        prediction.measurementName().value(),
        prediction.originalGravityValueKind(),
        PotentialAbvPredictionRestMapper.toResponse(prediction.prediction())
    );
  }
}
