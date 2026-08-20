package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.appleprofile.analysis.application.abv.contract.ApplePotentialAbvPredictionView;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.abv.contract.FeedstockPotentialAbvPredictionView;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionPotentialAbvPredictionView;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidatePotentialAbvView;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationPotentialAbvView;
import org.craft.mcs.cap.core.cideranalysis.abv.application.contract.PotentialAbvPredictionView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class PotentialAbvPredictionRestMapper {

  private PotentialAbvPredictionRestMapper() {
  }

  public static @NonNull PotentialAbvPredictionResponse toResponse(
      @NonNull PotentialAbvPredictionView prediction
  ) {
    requireNonNull(prediction, "potential ABV prediction cannot be null");
    return response(
        prediction.originalGravity().asBigDecimal(),
        prediction.predictedFinalGravity().asBigDecimal(),
        prediction.predictedFinalGravitySource().name(),
        prediction.potentialAbv().percent(),
        prediction.method().name(),
        prediction.confidence().name()
    );
  }

  public static @NonNull PotentialAbvPredictionResponse toResponse(
      @NonNull FeedstockPotentialAbvPredictionView prediction
  ) {
    requireNonNull(prediction, "feedstock potential ABV prediction cannot be null");
    return response(
        prediction.originalGravity().asBigDecimal(),
        prediction.predictedFinalGravity().asBigDecimal(),
        prediction.predictedFinalGravitySource().name(),
        prediction.potentialAbv().percent(),
        prediction.method().name(),
        prediction.confidence().name()
    );
  }

  public static @NonNull PotentialAbvPredictionResponse toResponse(
      @NonNull ApplePotentialAbvPredictionView prediction
  ) {
    requireNonNull(prediction, "apple potential ABV prediction cannot be null");
    return response(
        prediction.originalGravity().asBigDecimal(),
        prediction.predictedFinalGravity().asBigDecimal(),
        prediction.predictedFinalGravitySource().name(),
        prediction.potentialAbv().percent(),
        prediction.method().name(),
        prediction.confidence().name()
    );
  }

  public static @NonNull PotentialAbvPredictionResponse toResponse(
      @NonNull ProductionPotentialAbvPredictionView prediction
  ) {
    requireNonNull(prediction, "production potential ABV prediction cannot be null");
    return response(
        prediction.originalGravity().asBigDecimal(),
        prediction.predictedFinalGravity().asBigDecimal(),
        prediction.predictedFinalGravitySource().name(),
        prediction.potentialAbv().percent(),
        prediction.method().name(),
        prediction.confidence().name()
    );
  }

  public static @NonNull PotentialAbvPredictionResponse toResponse(
      @NonNull ProductionCandidatePotentialAbvView prediction
  ) {
    requireNonNull(prediction, "production candidate potential ABV prediction cannot be null");
    return response(
        prediction.originalGravity().asBigDecimal(),
        prediction.predictedFinalGravity().asBigDecimal(),
        prediction.predictedFinalGravitySource().name(),
        prediction.potentialAbv().percent(),
        prediction.method().name(),
        prediction.confidence().name()
    );
  }

  public static @NonNull PotentialAbvPredictionResponse toResponse(
      @NonNull FormulationPotentialAbvView prediction
  ) {
    requireNonNull(prediction, "formulation potential ABV prediction cannot be null");
    return response(
        prediction.originalGravity().asBigDecimal(),
        prediction.predictedFinalGravity().asBigDecimal(),
        prediction.predictedFinalGravitySource().name(),
        prediction.potentialAbv().percent(),
        prediction.method().name(),
        prediction.confidence().name()
    );
  }

  private static @NonNull PotentialAbvPredictionResponse response(
      @NonNull BigDecimal originalGravity,
      @NonNull BigDecimal predictedFinalGravity,
      @NonNull String predictedFinalGravitySource,
      @NonNull BigDecimal potentialAbv,
      @NonNull String method,
      @NonNull String confidence
  ) {
    return new PotentialAbvPredictionResponse(
        new DecimalQuantityResponse(originalGravity, CanonicalUnitLabels.SPECIFIC_GRAVITY),
        new DecimalQuantityResponse(predictedFinalGravity, CanonicalUnitLabels.SPECIFIC_GRAVITY),
        PotentialAbvPredictionResponse.FinalGravitySource.valueOf(predictedFinalGravitySource),
        new DecimalQuantityResponse(potentialAbv, CanonicalUnitLabels.ABV),
        PotentialAbvPredictionResponse.Method.valueOf(method),
        PotentialAbvPredictionResponse.Confidence.valueOf(confidence)
    );
  }
}
