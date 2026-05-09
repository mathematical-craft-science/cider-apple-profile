package org.craft.mcs.cap.infrastructure.adapter.in.rest.fermentation.mapper;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.fermentation.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.shared.domain.fermentation.PotentialAbvPrediction;
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
        prediction.assumedFinalGravity().asBigDecimal(),
        prediction.assumedFinalGravitySource().name(),
        prediction.potentialAbv().percent(),
        prediction.method().name(),
        prediction.confidence().name()
    );
  }
}
