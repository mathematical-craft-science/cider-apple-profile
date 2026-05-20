package org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ProfileTargetDistanceCandidateReference(
    @NonNull String type,
    @NonNull String id
) {

  public ProfileTargetDistanceCandidateReference {
    requireNonNull(type, "profile target distance candidate type cannot be null");
    requireNonNull(id, "profile target distance candidate id cannot be null");
    if (type.isBlank()) {
      throw new ApplicationValidationException("profile target distance candidate type cannot be blank");
    }
    if (id.isBlank()) {
      throw new ApplicationValidationException("profile target distance candidate id cannot be blank");
    }
  }
}
