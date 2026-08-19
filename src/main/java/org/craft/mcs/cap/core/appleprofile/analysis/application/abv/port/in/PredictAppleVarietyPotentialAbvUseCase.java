package org.craft.mcs.cap.core.appleprofile.analysis.application.abv.port.in;

import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.jspecify.annotations.NonNull;


public interface PredictAppleVarietyPotentialAbvUseCase {

  @NonNull
  AppleVarietyPotentialAbvPrediction predict(@NonNull AppleVarietyId varietyId);
}
