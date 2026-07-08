package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateScore;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateScores;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesCommand;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesUseCase;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Scores already-calculated profile points against one target objective.
 *
 * <p>This use case is used by graph/catalog scenarios where candidate points already exist and only
 * need distance ranking. Composition optimization is a different use case: it first generates
 * candidate source weights, calculates profiles, then scores them.
 */
@Component
@RequiredArgsConstructor
public final class ScoreProfileTargetDistanceCandidatesService
    implements ScoreProfileTargetDistanceCandidatesUseCase {

  private final @NonNull ProfileAxisNormalizerSelector normalizerSelector;
  private final @NonNull ProfileTargetScoringPurposeNormalizerPolicy normalizerPolicy;
  private final @NonNull ProfileTargetObjectiveFunctionFactory objectiveFunctionFactory;

  @Override
  public @NonNull ProfileTargetDistanceCandidateScores score(
      @NonNull ScoreProfileTargetDistanceCandidatesCommand command
  ) {
    requireNonNull(command, "score profile target distance candidates command cannot be null");
    final var normalizerCode = normalizerPolicy.normalizerFor(command.purpose());
    final var normalizer = normalizerSelector.select(normalizerCode);
    final var objectiveFunction = objectiveFunctionFactory.create(
        ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
        normalizer,
        command.weights()
    );
    final var scores = command.candidates().stream()
        .map(candidate -> new ProfileTargetDistanceCandidateScore(
            candidate.reference(),
            objectiveFunction.distanceTo(candidate.point(), command.objective())
        ))
        .sorted(ScoreProfileTargetDistanceCandidatesService::compareByDistance)
        .toList();

    return new ProfileTargetDistanceCandidateScores(command.purpose(), normalizerCode, scores);
  }

  private static int compareByDistance(
      @NonNull ProfileTargetDistanceCandidateScore left,
      @NonNull ProfileTargetDistanceCandidateScore right
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
