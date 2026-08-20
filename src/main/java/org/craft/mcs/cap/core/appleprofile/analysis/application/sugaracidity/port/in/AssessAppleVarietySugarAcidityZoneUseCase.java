package org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.port.in;

import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.contract.AppleVarietyGraphAnalysisView;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.jspecify.annotations.NonNull;


public interface AssessAppleVarietySugarAcidityZoneUseCase {

  @NonNull
  AppleVarietyGraphAnalysisView assess(@NonNull AppleVarietyId varietyId);
}
