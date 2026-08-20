package org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract;

import java.util.UUID;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Stable keyset position for ordered apple variety analysis pages. */
public record AppleVarietyAnalysisPagePosition(
    @NonNull String name,
    @NonNull UUID varietyId
) {

  public AppleVarietyAnalysisPagePosition {
    if (requireNonNull(name, "apple variety name cannot be null").isBlank()) {
      throw new ApplicationValidationException("apple variety name cannot be blank");
    }
    requireNonNull(varietyId, "apple variety id cannot be null");
  }
}
