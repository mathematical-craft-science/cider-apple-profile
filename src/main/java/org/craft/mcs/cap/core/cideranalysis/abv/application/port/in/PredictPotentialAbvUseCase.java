package org.craft.mcs.cap.core.cideranalysis.abv.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.abv.application.contract.PotentialAbvPredictionView;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;


/** Calculates potential ABV from a canonical original-gravity value. */
public interface PredictPotentialAbvUseCase {

  @NonNull
  PotentialAbvPredictionView predict(@NonNull SpecificGravityValue originalGravity);
}
