package org.craft.mcs.cap.shared.domain.fermentation;

import org.craft.mcs.cap.shared.domain.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class FixedFinalGravityPredictionStrategy
    implements FinalGravityPredictionStrategy {

  private final @NonNull SpecificGravityValue finalGravity;

  public FixedFinalGravityPredictionStrategy(@NonNull SpecificGravityValue finalGravity) {
    this.finalGravity = requireNonNull(finalGravity, "final gravity cannot be null");
  }

  public static @NonNull FixedFinalGravityPredictionStrategy dryCiderDefault() {
    return new FixedFinalGravityPredictionStrategy(SpecificGravityValue.of("1.000"));
  }

  @Override
  public @NonNull FinalGravityPrediction predictFinalGravity(
      @NonNull FinalGravityPredictionInput input
  ) {
    requireNonNull(input, "final gravity prediction input cannot be null");
    return new FinalGravityPrediction(
        finalGravity,
        FinalGravityPredictionSource.FIXED_DRY_CIDER_DEFAULT,
        PredictionConfidence.LOW
    );
  }
}
