package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record BlendProfileDistanceCandidateReference(
    @NonNull String type,
    @NonNull String id
) {

  public BlendProfileDistanceCandidateReference {
    requireNonNull(type, "blend profile distance candidate type cannot be null");
    requireNonNull(id, "blend profile distance candidate id cannot be null");
    if (type.isBlank()) {
      throw new IllegalArgumentException("blend profile distance candidate type cannot be blank");
    }
    if (id.isBlank()) {
      throw new IllegalArgumentException("blend profile distance candidate id cannot be blank");
    }
  }
}
