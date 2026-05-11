package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileObjective;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ScoreBlendProfileDistanceCandidatesCommand(
    @NonNull BlendProfileScoringPurpose purpose,
    @NonNull BlendProfileObjective objective,
    @NonNull BlendProfileObjectiveWeights weights,
    @NonNull List<@NonNull BlendProfileDistanceCandidate> candidates
) {

  public ScoreBlendProfileDistanceCandidatesCommand {
    requireNonNull(purpose, "blend profile scoring purpose cannot be null");
    requireNonNull(objective, "blend profile objective cannot be null");
    requireNonNull(weights, "blend profile objective weights cannot be null");
    candidates = List.copyOf(requireNonNull(candidates, "blend profile distance candidates cannot be null"));
  }

  public static @NonNull ScoreBlendProfileDistanceCandidatesCommand withEqualWeights(
      @NonNull BlendProfileScoringPurpose purpose,
      @NonNull BlendProfileObjective objective,
      @NonNull List<@NonNull BlendProfileDistanceCandidate> candidates
  ) {
    return new ScoreBlendProfileDistanceCandidatesCommand(
        purpose,
        objective,
        BlendProfileObjectiveWeights.balanced(),
        candidates
    );
  }
}
