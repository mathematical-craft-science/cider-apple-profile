package org.craft.mcs.cap.core.appleprofile.catalog.application.port.in;

import org.craft.mcs.cap.core.appleprofile.catalog.application.model.AppleVarietyReferenceProfileView;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.jspecify.annotations.NonNull;


/** Published catalog query for one immutable apple variety reference profile. */
public interface GetAppleVarietyReferenceProfileUseCase {

  @NonNull
  AppleVarietyReferenceProfileView get(@NonNull AppleVarietyId varietyId);
}
