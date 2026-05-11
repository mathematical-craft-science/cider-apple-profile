package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.point.SugarAcidityProfilePoint;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record BlendProfileDistanceCandidate(
    @NonNull BlendProfileDistanceCandidateReference reference,
    @NonNull SugarAcidityProfilePoint point
) {

  public BlendProfileDistanceCandidate {
    requireNonNull(reference, "blend profile distance candidate reference cannot be null");
    requireNonNull(point, "blend profile distance candidate point cannot be null");
  }
}
