package org.craft.mcs.cap.core.cideranalysis.abv.domain;

import org.jspecify.annotations.NonNull;


public interface FinalGravityPredictionStrategy {

  @NonNull
  FinalGravityPrediction predictFinalGravity(@NonNull FinalGravityPredictionInput input);
}
