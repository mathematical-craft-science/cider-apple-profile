package org.craft.mcs.cap.core.appleprofile.application.port.in;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record AppleVarietyCreated(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyName name
) {

  public AppleVarietyCreated {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(name, "apple variety name cannot be null");
  }
}
