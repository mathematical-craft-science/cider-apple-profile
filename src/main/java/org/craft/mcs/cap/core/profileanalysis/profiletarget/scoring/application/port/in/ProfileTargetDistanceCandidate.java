package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringInput;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ProfileTargetDistanceCandidate(
    @NonNull ProfileTargetDistanceCandidateReference reference,
    @NonNull ProfileTargetScoringInput input
) {

  public ProfileTargetDistanceCandidate {
    requireNonNull(reference, "profile target distance candidate reference cannot be null");
    requireNonNull(input, "profile target distance candidate input cannot be null");
  }
}
