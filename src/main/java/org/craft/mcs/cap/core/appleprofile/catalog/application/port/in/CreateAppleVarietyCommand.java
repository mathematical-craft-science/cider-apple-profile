package org.craft.mcs.cap.core.appleprofile.catalog.application.port.in;

import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyReferenceProfile;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.ReferenceProfileSource;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public record CreateAppleVarietyCommand(
    @NonNull AppleVarietyName name,
    @NonNull AppleVarietyReferenceProfile referenceProfile,
    @Nullable ReferenceProfileSource referenceProfileSource,
    @Nullable ConfidenceLevel referenceProfileConfidence
) {

  public CreateAppleVarietyCommand {
    requireNonNull(name, "apple variety name cannot be null");
    requireNonNull(referenceProfile, "apple variety reference profile cannot be null");
  }

  public CreateAppleVarietyCommand(
      @NonNull AppleVarietyName name,
      @NonNull AppleVarietyReferenceProfile referenceProfile
  ) {
    this(name, referenceProfile, null, null);
  }
}
