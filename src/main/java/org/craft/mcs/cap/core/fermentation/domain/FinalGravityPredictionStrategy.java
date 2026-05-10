package org.craft.mcs.cap.core.fermentation.domain;

import org.jspecify.annotations.NonNull;


public interface FinalGravityPredictionStrategy {

  @NonNull
  FinalGravityPrediction predictFinalGravity(@NonNull FinalGravityPredictionInput input);
}
