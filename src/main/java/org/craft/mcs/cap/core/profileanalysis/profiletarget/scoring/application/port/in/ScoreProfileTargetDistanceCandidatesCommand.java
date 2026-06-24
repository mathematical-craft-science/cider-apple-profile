package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ScoreProfileTargetDistanceCandidatesCommand(
    @NonNull ProfileTargetScoringPurpose purpose,
    @NonNull ProfileTargetObjective objective,
    @NonNull ProfileTargetObjectiveWeights weights,
    @NonNull List<@NonNull ProfileTargetDistanceCandidate> candidates
) {

  public ScoreProfileTargetDistanceCandidatesCommand {
    requireNonNull(purpose, "profile target scoring purpose cannot be null");
    requireNonNull(objective, "profile target objective cannot be null");
    requireNonNull(weights, "profile target objective weights cannot be null");
    candidates = List.copyOf(requireNonNull(candidates, "profile target distance candidates cannot be null"));
  }

  public static @NonNull ScoreProfileTargetDistanceCandidatesCommand withEqualWeights(
      @NonNull ProfileTargetScoringPurpose purpose,
      @NonNull ProfileTargetObjective objective,
      @NonNull List<@NonNull ProfileTargetDistanceCandidate> candidates
  ) {
    return new ScoreProfileTargetDistanceCandidatesCommand(
        purpose,
        objective,
        ProfileTargetObjectiveWeights.balanced(),
        candidates
    );
  }
}
