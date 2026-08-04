package org.craft.mcs.cap.core.profileanalysis.abv.domain;

import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record FinalGravityPredictionInput(
    @NonNull SpecificGravityValue originalGravity
) {

  public FinalGravityPredictionInput {
    requireNonNull(originalGravity, "original gravity cannot be null");
  }
}
