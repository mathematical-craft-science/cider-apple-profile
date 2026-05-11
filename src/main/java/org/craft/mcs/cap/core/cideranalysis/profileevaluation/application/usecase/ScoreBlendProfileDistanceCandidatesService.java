package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.BlendProfileDistanceCandidateScore;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.BlendProfileDistanceCandidateScores;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.ScoreBlendProfileDistanceCandidatesCommand;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.ScoreBlendProfileDistanceCandidatesUseCase;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.DistanceToTargetAreaBlendProfileObjectiveFunction;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class ScoreBlendProfileDistanceCandidatesService
    implements ScoreBlendProfileDistanceCandidatesUseCase {

  private final @NonNull BlendProfileAxisNormalizerSelector normalizerSelector;
  private final @NonNull BlendProfileScoringPurposeNormalizerPolicy normalizerPolicy;

  @Override
  public @NonNull BlendProfileDistanceCandidateScores score(
      @NonNull ScoreBlendProfileDistanceCandidatesCommand command
  ) {
    requireNonNull(command, "score blend profile distance candidates command cannot be null");
    final var normalizerCode = normalizerPolicy.normalizerFor(command.purpose());
    final var normalizer = normalizerSelector.select(normalizerCode);
    final var objectiveFunction = new DistanceToTargetAreaBlendProfileObjectiveFunction(
        normalizer,
        command.weights()
    );
    final var scores = command.candidates().stream()
        .map(candidate -> new BlendProfileDistanceCandidateScore(
            candidate.reference(),
            objectiveFunction.distanceTo(candidate.point(), command.objective())
        ))
        .sorted(ScoreBlendProfileDistanceCandidatesService::compareByDistance)
        .toList();

    return new BlendProfileDistanceCandidateScores(command.purpose(), normalizerCode, scores);
  }

  private static int compareByDistance(
      @NonNull BlendProfileDistanceCandidateScore left,
      @NonNull BlendProfileDistanceCandidateScore right
  ) {
    final var leftValue = left.distance().value();
    final var rightValue = right.distance().value();
    if (leftValue == null && rightValue == null) {
      return 0;
    }
    if (leftValue == null) {
      return 1;
    }
    if (rightValue == null) {
      return -1;
    }
    return leftValue.compareTo(rightValue);
  }
}
