package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ProfileTargetDistanceCandidateScores(
    @NonNull ProfileTargetScoringPurpose purpose,
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @NonNull List<@NonNull ProfileTargetDistanceCandidateScore> scores
) {

  public ProfileTargetDistanceCandidateScores {
    requireNonNull(purpose, "profile target scoring purpose cannot be null");
    requireNonNull(normalizerCode, "profile axis normalizer code cannot be null");
    scores = List.copyOf(requireNonNull(scores, "profile target distance candidate scores cannot be null"));
  }
}
