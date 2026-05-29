package org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.usecase;

import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesCommand;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetScore;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetScores;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.ScoreFeedstockBatchProfileTargetCommand;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.ScoreFeedstockBatchProfileTargetUseCase;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Scores current feedstock batch profiles against a selected target area.
 *
 * <p>This service adapts the feedstock workspace to the generic profile-target scoring use case.
 * It selects only the latest measurement per batch, filters unplottable or non-matching profiles,
 * resolves either a predefined zone target or a custom rectangle, then ranks candidates by target
 * distance. It does not generate new compositions and does not add screen-specific analysis
 * enrichments such as contribution roles.
 */
@Component
@RequiredArgsConstructor
public final class ScoreFeedstockBatchProfileTargetService
    implements ScoreFeedstockBatchProfileTargetUseCase {

  private final @NonNull FeedstockBatchProfileTargetScorableCandidateProvider candidateProvider;
  private final @NonNull FeedstockProfileTargetObjectiveResolver objectiveResolver;
  private final @NonNull ScoreProfileTargetDistanceCandidatesUseCase scoreProfileTargetDistanceCandidatesUseCase;

  @Override
  public @NonNull FeedstockBatchProfileTargetScores score(
      @NonNull ScoreFeedstockBatchProfileTargetCommand command
  ) {
    requireNonNull(command, "score feedstock batch profile target command cannot be null");

    final var candidates = candidateProvider.findCandidates(command);
    final var candidatesByReference = candidates.stream()
        .collect(Collectors.toMap(
            candidate -> candidate.candidate().reference().id(),
            Function.identity()
        ));
    final var scores = scoreProfileTargetDistanceCandidatesUseCase.score(
        new ScoreProfileTargetDistanceCandidatesCommand(
            command.purpose(),
            objectiveResolver.resolve(command),
            command.weights(),
            candidates.stream()
                .map(FeedstockBatchProfileTargetScorableCandidate::candidate)
                .toList()
        )
    );

    return new FeedstockBatchProfileTargetScores(
        scores.purpose(),
        scores.normalizerCode(),
        scores.scores().stream()
            .map(score -> {
              final var candidate = requireNonNull(
                  candidatesByReference.get(score.reference().id()),
                  "feedstock measurement candidate is missing for score"
              );
              final var measurement = candidate.measurement();
              return new FeedstockBatchProfileTargetScore(
                  measurement.batchId(),
                  command.ciderMakerId(),
                  measurement.measurementId(),
                  measurement.name(),
                  candidate.candidate().point(),
                  score.distance()
              );
            })
            .toList()
    );
  }
}
