package org.craft.mcs.cap.shared.domain.fermentation;

import org.jspecify.annotations.NonNull;


public interface AbvPredictionStrategy {

  @NonNull
  AbvPredictionMethod method();

  @NonNull
  AbvValue predict(@NonNull AbvPredictionInput input);
}
