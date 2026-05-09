package org.craft.mcs.cap.shared.domain.fermentation;

import org.craft.mcs.cap.shared.domain.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.domain.sugar.SugarConcentration;
import org.jmolecules.ddd.annotation.Service;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


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
      @NonNull SpecificGravityValue assumedFinalGravity
  ) {
    return predict(
        originalGravity,
        new FinalGravityPrediction(
            assumedFinalGravity,
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

    final var assumedFinalGravity = finalGravityPrediction.finalGravity();

    if (assumedFinalGravity.compareTo(originalGravity) > 0) {
      throw new IllegalArgumentException("assumed final gravity must not exceed original gravity");
    }

    return new PotentialAbvPrediction(
        originalGravity,
        assumedFinalGravity,
        finalGravityPrediction.source(),
        abvPredictionStrategy.predict(new AbvPredictionInput(originalGravity, assumedFinalGravity)),
        abvPredictionStrategy.method(),
        finalGravityPrediction.confidence()
    );
  }
}
