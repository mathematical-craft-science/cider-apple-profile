package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in;

import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.model.AppleVarietyGraphAnalysisView;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.jspecify.annotations.NonNull;


public interface AssessAppleVarietySugarAcidityZoneUseCase {

  @NonNull
  AppleVarietyGraphAnalysisView assess(@NonNull AppleVarietyId varietyId);
}
