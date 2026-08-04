package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ProfileTargetDistanceCandidateScores(
    @NonNull ProfileTargetScoringConfiguration configuration,
    @NonNull List<@NonNull ProfileTargetDistanceCandidateScore> scores
) {

  public ProfileTargetDistanceCandidateScores {
    requireNonNull(configuration, "profile target scoring configuration cannot be null");
    scores = List.copyOf(requireNonNull(scores, "profile target distance candidate scores cannot be null"));
  }
}
