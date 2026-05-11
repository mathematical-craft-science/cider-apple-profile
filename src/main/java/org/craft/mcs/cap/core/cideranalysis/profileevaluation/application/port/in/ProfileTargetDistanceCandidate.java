package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.point.SugarAcidityProfilePoint;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ProfileTargetDistanceCandidate(
    @NonNull ProfileTargetDistanceCandidateReference reference,
    @NonNull SugarAcidityProfilePoint point
) {

  public ProfileTargetDistanceCandidate {
    requireNonNull(reference, "profile target distance candidate reference cannot be null");
    requireNonNull(point, "profile target distance candidate point cannot be null");
  }
}
