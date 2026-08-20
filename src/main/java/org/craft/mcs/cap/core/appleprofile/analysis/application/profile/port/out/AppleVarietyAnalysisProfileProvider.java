package org.craft.mcs.cap.core.appleprofile.analysis.application.profile.port.out;

import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisProfile;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.jspecify.annotations.NonNull;


/** Consumer-owned port for obtaining one immutable catalog profile as analysis input. */
public interface AppleVarietyAnalysisProfileProvider {

  @NonNull
  AppleVarietyAnalysisProfile get(@NonNull AppleVarietyId varietyId);
}
