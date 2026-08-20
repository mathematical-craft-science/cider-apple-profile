package org.craft.mcs.cap.support;

import org.craft.mcs.cap.core.appleprofile.analysis.application.abv.contract.ApplePotentialAbvPredictionView;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidatePotentialAbvView;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionPotentialAbvPredictionView;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;


/** Deterministic potential-ABV fixtures using the production default strategies. */
public final class PotentialAbvPredictionFixtures {

  private static final PotentialAbvPredictor PREDICTOR = new PotentialAbvPredictor(
      FixedFinalGravityPredictionStrategy.dryCiderDefault(),
      new StandardGravityDropAbvPredictionStrategy()
  );

  private PotentialAbvPredictionFixtures() {
  }

  public static @NonNull PotentialAbvPredictor predictor() {
    return PREDICTOR;
  }

  public static @NonNull PotentialAbvPrediction prediction(@NonNull SpecificGravityValue originalGravity) {
    return PREDICTOR.predict(originalGravity);
  }

  public static @NonNull ApplePotentialAbvPredictionView applePrediction(
      @NonNull SpecificGravityValue originalGravity
  ) {
    final var prediction = PREDICTOR.predict(originalGravity);
    return new ApplePotentialAbvPredictionView(
        prediction.originalGravity(),
        prediction.predictedFinalGravity(),
        ApplePotentialAbvPredictionView.FinalGravitySource.valueOf(
            prediction.predictedFinalGravitySource().name()
        ),
        prediction.potentialAbv(),
        ApplePotentialAbvPredictionView.Method.valueOf(prediction.method().name()),
        ApplePotentialAbvPredictionView.Confidence.valueOf(prediction.confidence().name())
    );
  }

  public static @NonNull ProductionCandidatePotentialAbvView productionCandidatePrediction(
      @NonNull SpecificGravityValue originalGravity
  ) {
    final var prediction = PREDICTOR.predict(originalGravity);
    return new ProductionCandidatePotentialAbvView(
        prediction.originalGravity(),
        prediction.predictedFinalGravity(),
        ProductionCandidatePotentialAbvView.FinalGravitySource.valueOf(
            prediction.predictedFinalGravitySource().name()
        ),
        prediction.potentialAbv(),
        ProductionCandidatePotentialAbvView.Method.valueOf(prediction.method().name()),
        ProductionCandidatePotentialAbvView.Confidence.valueOf(prediction.confidence().name())
    );
  }

  public static @NonNull ProductionPotentialAbvPredictionView productionPrediction(
      @NonNull SpecificGravityValue originalGravity
  ) {
    final var prediction = PREDICTOR.predict(originalGravity);
    return new ProductionPotentialAbvPredictionView(
        prediction.originalGravity(),
        prediction.predictedFinalGravity(),
        ProductionPotentialAbvPredictionView.FinalGravitySource.valueOf(
            prediction.predictedFinalGravitySource().name()
        ),
        prediction.potentialAbv(),
        ProductionPotentialAbvPredictionView.Method.valueOf(prediction.method().name()),
        ProductionPotentialAbvPredictionView.Confidence.valueOf(prediction.confidence().name())
    );
  }
}
