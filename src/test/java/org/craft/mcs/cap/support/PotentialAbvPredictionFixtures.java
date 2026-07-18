package org.craft.mcs.cap.support;

import org.craft.mcs.cap.core.profileanalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
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
}
