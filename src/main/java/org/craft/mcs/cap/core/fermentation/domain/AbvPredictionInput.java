package org.craft.mcs.cap.core.fermentation.domain;

import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record AbvPredictionInput(
    @NonNull SpecificGravityValue originalGravity,
    @NonNull SpecificGravityValue finalGravity
) {

  public AbvPredictionInput {
    requireNonNull(originalGravity, "original gravity cannot be null");
    requireNonNull(finalGravity, "final gravity cannot be null");

    if (finalGravity.compareTo(originalGravity) > 0) {
      throw new IllegalArgumentException("final gravity must not exceed original gravity");
    }
  }
}
