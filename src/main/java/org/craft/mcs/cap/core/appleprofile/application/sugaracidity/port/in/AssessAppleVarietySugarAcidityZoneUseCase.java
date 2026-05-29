package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.jspecify.annotations.NonNull;


public interface AssessAppleVarietySugarAcidityZoneUseCase {

  @NonNull
  AppleVarietySugarAcidityZoneResult assess(@NonNull AppleVarietyId varietyId);
}
