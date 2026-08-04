package org.craft.mcs.cap.core.profileanalysis.capability.application.model;

import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisCapability;
import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisInputAvailability;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Optional filter for one profile-analysis capability and its expected availability. */
public record ProfileAnalysisCapabilityCriteria(
    @NonNull ProfileAnalysisCapability capability,
    @Nullable Boolean capable
) {

  public ProfileAnalysisCapabilityCriteria {
    requireNonNull(capability, "profile analysis capability cannot be null");
  }

  public static @NonNull ProfileAnalysisCapabilityCriteria capable(
      @NonNull ProfileAnalysisCapability capability
  ) {
    return new ProfileAnalysisCapabilityCriteria(capability, true);
  }

  public static @NonNull ProfileAnalysisCapabilityCriteria notCapable(
      @NonNull ProfileAnalysisCapability capability
  ) {
    return new ProfileAnalysisCapabilityCriteria(capability, false);
  }

  public boolean matches(boolean actualCapability) {
    return capable == null || capable == actualCapability;
  }

  /** Matches this optional expectation against source-neutral input-availability facts. */
  public boolean matches(@NonNull ProfileAnalysisInputAvailability availability) {
    return matches(requireNonNull(availability, "profile analysis input availability cannot be null")
        .supports(capability));
  }
}
