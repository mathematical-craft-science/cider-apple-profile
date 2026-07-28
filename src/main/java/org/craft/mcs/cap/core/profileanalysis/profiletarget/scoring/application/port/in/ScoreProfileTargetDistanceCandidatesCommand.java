package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetIntervalPolicy;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ScoreProfileTargetDistanceCandidatesCommand(
    @NonNull ProfileTargetObjective objective,
    @NonNull ProfileTargetScoringConfiguration configuration,
    @NonNull ProfileTargetIntervalPolicy intervalPolicy,
    @NonNull List<@NonNull ProfileTargetDistanceCandidate> candidates
) {

  public ScoreProfileTargetDistanceCandidatesCommand {
    requireNonNull(objective, "profile target objective cannot be null");
    requireNonNull(configuration, "profile target scoring configuration cannot be null");
    requireNonNull(intervalPolicy, "profile target interval policy cannot be null");
    candidates = List.copyOf(requireNonNull(candidates, "profile target distance candidates cannot be null"));
  }

  public ScoreProfileTargetDistanceCandidatesCommand(
      @NonNull ProfileTargetObjective objective,
      @NonNull ProfileTargetScoringConfiguration configuration,
      @NonNull List<@NonNull ProfileTargetDistanceCandidate> candidates
  ) {
    this(
        objective,
        configuration,
        ProfileTargetIntervalPolicy.MIDPOINT,
        candidates
    );
  }
}
