package org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.usecase;

import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessmentRequest;
import org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain.CompositionContributionRoleAssessor;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesCommand;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ScoreProfileTargetDistanceCandidatesUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetCandidate;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetCandidates;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.QueryFeedstockBatchProfileTargetCandidatesUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.ScoreFeedstockBatchProfileTargetCommand;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Analysis read model for target-aware feedstock batch candidates.
 *
 * <p>This use case is intentionally separate from the generic feedstock profile-target score
 * endpoint. It enriches distance-scored current batch profiles with contribution roles for
 * target-aware analysis clients where a technologist evaluates source material against a selected
 * target.
 */
@Component
@RequiredArgsConstructor
public final class QueryFeedstockBatchProfileTargetCandidatesService
    implements QueryFeedstockBatchProfileTargetCandidatesUseCase {

  private final @NonNull FeedstockBatchProfileTargetScorableCandidateProvider candidateProvider;
  private final @NonNull FeedstockProfileTargetObjectiveResolver objectiveResolver;
  private final @NonNull ScoreProfileTargetDistanceCandidatesUseCase scoreProfileTargetDistanceCandidatesUseCase;
  private final @NonNull CompositionContributionRoleAssessor contributionRoleAssessor;

  @Override
  public @NonNull FeedstockBatchProfileTargetCandidates query(
      @NonNull FeedstockBatchProfileTargetCandidateQuery query
  ) {
    requireNonNull(query, "feedstock batch profile target candidate query cannot be null");

    final var command = toScoreCommand(query);
    final var candidates = candidateProvider.findCandidates(command);
    final var objective = objectiveResolver.resolve(command);
    final var candidatesByReference = candidates.stream()
        .collect(Collectors.toMap(
            candidate -> candidate.candidate().reference().id(),
            Function.identity()
        ));
    final var scores = scoreProfileTargetDistanceCandidatesUseCase.score(
        new ScoreProfileTargetDistanceCandidatesCommand(
            command.purpose(),
            objective,
            command.weights(),
            candidates.stream()
                .map(FeedstockBatchProfileTargetScorableCandidate::candidate)
                .toList()
        )
    );

    return new FeedstockBatchProfileTargetCandidates(
        scores.purpose(),
        scores.normalizerCode(),
        scores.scores().stream()
            .map(score -> {
              final var candidate = requireNonNull(
                  candidatesByReference.get(score.reference().id()),
                  "feedstock measurement candidate is missing for score"
              );
              final var measurement = candidate.measurement();
              final var roleAssessmentRequest = new CompositionContributionRoleAssessmentRequest(
                  candidate.axes(),
                  objective
              );
              return new FeedstockBatchProfileTargetCandidate(
                  measurement.batchId(),
                  measurement.measurementId(),
                  measurement.name(),
                  candidate.candidate().point(),
                  score.distance(),
                  contributionRoleAssessor.assess(roleAssessmentRequest)
              );
            })
            .toList()
    );
  }

  private static @NonNull ScoreFeedstockBatchProfileTargetCommand toScoreCommand(
      @NonNull FeedstockBatchProfileTargetCandidateQuery query
  ) {
    return new ScoreFeedstockBatchProfileTargetCommand(
        query.ciderMakerId(),
        query.filters(),
        query.purpose(),
        query.target(),
        query.tanninTarget(),
        query.weights(),
        query.limit()
    );
  }
}
