package org.craft.mcs.cap.shared.domain.fermentation;

import org.jspecify.annotations.NonNull;


public interface FinalGravityPredictionStrategy {

  @NonNull
  FinalGravityPrediction predictFinalGravity(@NonNull FinalGravityPredictionInput input);
}
