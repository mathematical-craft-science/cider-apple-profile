package org.craft.mcs.cap.core.fermentation.domain;

import org.jspecify.annotations.NonNull;


public interface AbvPredictionStrategy {

  @NonNull
  AbvPredictionMethod method();

  @NonNull
  AbvValue predict(@NonNull AbvPredictionInput input);
}
