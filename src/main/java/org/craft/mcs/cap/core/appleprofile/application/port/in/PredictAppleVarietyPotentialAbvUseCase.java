package org.craft.mcs.cap.core.appleprofile.application.port.in;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.jspecify.annotations.NonNull;


public interface PredictAppleVarietyPotentialAbvUseCase {

  @NonNull
  AppleVarietyPotentialAbvPrediction predict(@NonNull AppleVarietyId varietyId);
}
