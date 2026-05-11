package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileTargetDistance;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record BlendProfileDistanceCandidateScore(
    @NonNull BlendProfileDistanceCandidateReference reference,
    @NonNull BlendProfileTargetDistance distance
) {

  public BlendProfileDistanceCandidateScore {
    requireNonNull(reference, "blend profile distance candidate reference cannot be null");
    requireNonNull(distance, "blend profile target distance cannot be null");
  }
}
