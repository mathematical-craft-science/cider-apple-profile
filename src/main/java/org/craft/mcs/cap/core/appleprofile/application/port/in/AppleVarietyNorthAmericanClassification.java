package org.craft.mcs.cap.core.appleprofile.application.port.in;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.cideranalysis.classification.profile.northamerican.NorthAmericanProfile;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record AppleVarietyNorthAmericanClassification(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyName varietyName,
    @NonNull NorthAmericanProfile profile
) {

  public AppleVarietyNorthAmericanClassification {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(varietyName, "apple variety name cannot be null");
    requireNonNull(profile, "north american profile cannot be null");
  }
}
