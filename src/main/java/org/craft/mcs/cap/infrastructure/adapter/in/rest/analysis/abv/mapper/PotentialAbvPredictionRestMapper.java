package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
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
        new DecimalQuantityResponse(prediction.originalGravity().asBigDecimal(), CanonicalUnitLabels.SPECIFIC_GRAVITY),
        new DecimalQuantityResponse(
            prediction.predictedFinalGravity().asBigDecimal(),
            CanonicalUnitLabels.SPECIFIC_GRAVITY
        ),
        prediction.predictedFinalGravitySource(),
        new DecimalQuantityResponse(prediction.potentialAbv().percent(), CanonicalUnitLabels.ABV),
        prediction.method(),
        prediction.confidence()
    );
  }
}
