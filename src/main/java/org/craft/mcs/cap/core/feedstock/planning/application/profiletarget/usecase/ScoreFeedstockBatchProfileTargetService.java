package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.usecase;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.planning.application.authorization.port.out.FeedstockPlanningAccessControl;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetScore;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetScores;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetQuery;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.ScoreFeedstockBatchProfileTargetUseCase;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.out.FeedstockTargetFitAnalyzer;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic.SPECIFIC_GRAVITY;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic.TANNIN;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic.TITRATABLE_ACIDITY;


/**
 * Scores current feedstock batch profiles against a selected target area.
 *
 * <p>This service adapts state-aware current Feedstock profiles to the generic profile-target
 * scoring use case. It filters unplottable or non-matching profiles, resolves either a predefined
 * zone target or a custom rectangle, then ranks candidates by target distance. It does not
 * generate compositions or add screen-specific enrichments such as contribution roles.
 */
@Component
@RequiredArgsConstructor
public final class ScoreFeedstockBatchProfileTargetService
    implements ScoreFeedstockBatchProfileTargetUseCase {

  private final @NonNull FeedstockBatchProfileTargetScorableCandidateProvider candidateProvider;
  private final @NonNull FeedstockTargetFitAnalyzer targetFitAnalyzer;
  private final @NonNull FeedstockPlanningAccessControl accessControl;

  @Override
  public @NonNull FeedstockBatchProfileTargetScores score(
      @NonNull FeedstockBatchProfileTargetQuery query
  ) {
    requireNonNull(query, "feedstock batch profile target query cannot be null");
    accessControl.requireReadAccess(query.workspaceId());

    final var candidates = candidateProvider.findCandidates(query);
    final var candidatesByReference = candidates.stream()
        .collect(Collectors.toMap(
            candidate -> candidate.candidate().reference(),
            Function.identity()
        ));
    final var scores = targetFitAnalyzer.score(
        new FeedstockTargetFitAnalyzer.Request(
            query.target(),
            query.tanninTarget(),
            new FeedstockTargetFitConfiguration(query.analysisScenario(), query.weights()),
            candidates.stream().map(FeedstockBatchProfileTargetScorableCandidate::candidate).toList()
        )
    );

    return new FeedstockBatchProfileTargetScores(
        query.analysisScenario(),
        scores.normalizer(),
        scores.scores().stream()
            .map(score -> {
              final var candidate = requireNonNull(
                  candidatesByReference.get(score.reference()),
                  "feedstock current profile candidate is missing for score"
              );
              final var currentProfile = candidate.currentProfile();
              return new FeedstockBatchProfileTargetScore(
                  currentProfile.batchId(),
                  query.workspaceId(),
                  currentProfile.currentMaterialStateId(),
                  currentProfile.evidenceFor(Set.of(
                      SPECIFIC_GRAVITY,
                      TITRATABLE_ACIDITY,
                      TANNIN
                  )),
                  candidate.candidate().characteristics(),
                  score.distance(),
                  score.contribution()
              );
            })
            .toList()
    );
  }
}
