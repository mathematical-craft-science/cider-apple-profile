package org.craft.mcs.cap.core.profileanalysis.abv.domain;

import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.jspecify.annotations.NonNull;


/**
 * Strategy for calculating ABV from original and final gravity.
 *
 * <p>The strategy is separated from final-gravity prediction so future yeast-aware or fermentation
 * model inputs can change expected FG without replacing the ABV formula itself.
 */
public interface AbvPredictionStrategy {

  @NonNull
  AbvPredictionMethod method();

  @NonNull
  AbvValue predict(@NonNull AbvPredictionInput input);
}
