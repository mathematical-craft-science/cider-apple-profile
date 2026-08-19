package org.craft.mcs.cap.shared.testing;

import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyReferenceProfile;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Converts typed test fixtures to apple catalog reference characteristics. */
public final class AppleVarietyReferenceProfiles {

  private AppleVarietyReferenceProfiles() {
  }

  public static @NonNull AppleVarietyReferenceProfile from(@NonNull ProfileCharacteristicsFixture profile) {
    requireNonNull(profile, "profile characteristics fixture cannot be null");
    return new AppleVarietyReferenceProfile(
        profile.specificGravity(),
        profile.titratableAcidity(),
        profile.ph(),
        profile.tannin()
    );
  }
}
