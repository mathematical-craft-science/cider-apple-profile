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
    return predict(
        originalGravity,
        finalGravityPredictionStrategy.predictFinalGravity(
            new FinalGravityPredictionInput(originalGravity)
        )
    );
  }

  public @NonNull PotentialAbvPrediction predict(
      @NonNull SpecificGravityValue originalGravity,
      @NonNull SpecificGravityValue assumedFinalGravity
  ) {
    requireNonNull(originalGravity, "original gravity cannot be null");
    requireNonNull(assumedFinalGravity, "assumed final gravity cannot be null");

    if (assumedFinalGravity.compareTo(originalGravity) > 0) {
      throw new IllegalArgumentException("assumed final gravity must not exceed original gravity");
    }

    return new PotentialAbvPrediction(
        originalGravity,
        assumedFinalGravity,
        abvPredictionStrategy.predict(new AbvPredictionInput(originalGravity, assumedFinalGravity)),
        abvPredictionStrategy.method()
    );
  }
}
