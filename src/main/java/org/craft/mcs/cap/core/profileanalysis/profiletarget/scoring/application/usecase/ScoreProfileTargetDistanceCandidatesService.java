package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.usecase;

import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateScore;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateScores;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesCommand;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesUseCase;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistanceOrdering;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionFactory;
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
  private final @NonNull ProfileTargetObjectiveFunctionFactory objectiveFunctionFactory;

  @Override
  public @NonNull ProfileTargetDistanceCandidateScores score(
      @NonNull ScoreProfileTargetDistanceCandidatesCommand command
  ) {
    requireNonNull(command, "score profile target distance candidates command cannot be null");
    final var configuration = command.configuration();
    final var normalizer = normalizerSelector.select(configuration.normalizerCode());
    final var objectiveFunction = objectiveFunctionFactory.create(
        configuration.objectiveFunctionCode(),
        normalizer,
        configuration.weights()
    );
    final var scores = command.candidates().stream()
        .map(candidate -> new ProfileTargetDistanceCandidateScore(
            candidate.reference(),
            objectiveFunction.distanceTo(
                candidate.input(),
                command.intervalPolicy(),
                command.objective()
            )
        ))
        .sorted(Comparator.comparing(
            ProfileTargetDistanceCandidateScore::distance,
            ProfileTargetDistanceOrdering::compare
        ))
        .toList();

    return new ProfileTargetDistanceCandidateScores(configuration, scores);
  }
}
