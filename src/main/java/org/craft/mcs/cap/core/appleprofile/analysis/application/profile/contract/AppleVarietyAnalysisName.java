package org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Analysis-owned display name copied from the catalog reference profile at the ACL boundary. */
public record AppleVarietyAnalysisName(@NonNull String value) {

  public AppleVarietyAnalysisName {
    requireNonNull(value, "apple variety analysis name cannot be null");
  }

  public static @NonNull AppleVarietyAnalysisName of(@NonNull String value) {
    return new AppleVarietyAnalysisName(value);
  }
}
