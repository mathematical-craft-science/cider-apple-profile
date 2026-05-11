package org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetDistance;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ProfileTargetDistanceCandidateScore(
    @NonNull ProfileTargetDistanceCandidateReference reference,
    @NonNull ProfileTargetDistance distance
) {

  public ProfileTargetDistanceCandidateScore {
    requireNonNull(reference, "profile target distance candidate reference cannot be null");
    requireNonNull(distance, "profile target distance cannot be null");
  }
}
