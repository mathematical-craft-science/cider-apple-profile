package org.craft.mcs.cap.core.fermentation.domain;

import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jmolecules.ddd.annotation.Service;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Domain service that coordinates potential ABV prediction.
 *
 * <p>The predictor first obtains or accepts a predicted final gravity, then delegates the actual ABV
 * formula to the selected {@link AbvPredictionStrategy}. This keeps the extension point explicit:
 * future yeast-aware strategies should replace final-gravity prediction, while the gravity-drop ABV
 * formula can remain stable unless the domain requires another method.
 */
@Service
public final class PotentialAbvPredictor {

  private final @NonNull FinalGravityPredictionStrategy finalGravityPredictionStrategy;
  private final @NonNull AbvPredictionStrategy abvPredictionStrategy;

  public PotentialAbvPredictor(
      @NonNull FinalGravityPredictionStrategy finalGravityPredictionStrategy,
      @NonNull AbvPredictionStrategy abvPredictionStrategy
  ) {
    this.finalGravityPredictionStrategy = requireNonNull(
        finalGravityPredictionStrategy,
        "final gravity prediction strategy cannot be null"
    );
    this.abvPredictionStrategy = requireNonNull(
        abvPredictionStrategy,
        "ABV prediction strategy cannot be null"
    );
  }

  public @NonNull PotentialAbvPrediction predict(
      @NonNull SugarConcentration sugar
  ) {
    requireNonNull(sugar, "sugar concentration cannot be null");
    final var originalGravity = sugar.specificGravity();
    final var finalGravityPrediction = finalGravityPredictionStrategy.predictFinalGravity(
        new FinalGravityPredictionInput(originalGravity)
    );
    return predict(
        originalGravity,
        finalGravityPrediction
    );
  }

  public @NonNull PotentialAbvPrediction predict(
      @NonNull SpecificGravityValue originalGravity,
      @NonNull SpecificGravityValue predictedFinalGravity
  ) {
    return predict(
        originalGravity,
        new FinalGravityPrediction(
            predictedFinalGravity,
            FinalGravityPredictionSource.FIXED_DRY_CIDER_DEFAULT,
            PredictionConfidence.LOW
        )
    );
  }

  private @NonNull PotentialAbvPrediction predict(
      @NonNull SpecificGravityValue originalGravity,
      @NonNull FinalGravityPrediction finalGravityPrediction
  ) {
    requireNonNull(originalGravity, "original gravity cannot be null");
    requireNonNull(finalGravityPrediction, "final gravity prediction cannot be null");

    final var predictedFinalGravity = finalGravityPrediction.finalGravity();

    if (predictedFinalGravity.compareTo(originalGravity) > 0) {
      throw new IllegalArgumentException("predicted final gravity must not exceed original gravity");
    }

    return new PotentialAbvPrediction(
        originalGravity,
        predictedFinalGravity,
        finalGravityPrediction.source(),
        abvPredictionStrategy.predict(new AbvPredictionInput(originalGravity, predictedFinalGravity)),
        abvPredictionStrategy.method(),
        finalGravityPrediction.confidence()
    );
  }
}
