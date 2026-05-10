package org.craft.mcs.cap.infrastructure.adapter.in.rest.fermentation.mapper;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.fermentation.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.core.fermentation.domain.PotentialAbvPrediction;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class PotentialAbvPredictionRestMapper {

  private PotentialAbvPredictionRestMapper() {
  }

  public static @NonNull PotentialAbvPredictionResponse toResponse(
      @NonNull PotentialAbvPrediction prediction
  ) {
    requireNonNull(prediction, "potential ABV prediction cannot be null");

    return new PotentialAbvPredictionResponse(
        prediction.originalGravity().asBigDecimal(),
        prediction.predictedFinalGravity().asBigDecimal(),
        prediction.predictedFinalGravitySource(),
        prediction.potentialAbv().percent(),
        prediction.method(),
        prediction.confidence()
    );
  }
}
