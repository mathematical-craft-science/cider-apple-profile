package org.craft.mcs.cap.shared.domain.fermentation;

import org.craft.mcs.cap.shared.domain.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;


public interface FinalGravityPredictionStrategy {

  @NonNull
  SpecificGravityValue predictFinalGravity(@NonNull FinalGravityPredictionInput input);
}
