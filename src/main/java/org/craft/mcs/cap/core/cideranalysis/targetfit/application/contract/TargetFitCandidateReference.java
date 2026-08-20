package org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Opaque caller reference retained while target-fit candidates are scored. */
public record TargetFitCandidateReference(
    @NonNull String type,
    @NonNull String id
) {

  public TargetFitCandidateReference {
    requireNonNull(type, "target candidate reference type cannot be null");
    requireNonNull(id, "target candidate reference id cannot be null");
    if (type.isBlank() || id.isBlank()) {
      throw new ApplicationValidationException("target candidate reference cannot be blank");
    }
  }
}
