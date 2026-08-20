package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.usecase;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.contract.FeedstockPlanningCandidatePagePosition;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.contract.FeedstockPlanningCandidateQuery;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.contract.FeedstockPlanningCandidateSnapshot;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.port.out.FeedstockPlanningCandidateProvider;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.port.out.FeedstockPlanningGraphAnalyzer;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetCharacteristics;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockBatchProfileTargetQuery;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.out.FeedstockTargetFitAnalyzer;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.shared.application.query.pagination.ChunkedCursorPageCollector;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Selects current Feedstock material states that can participate in profile-target scoring.
 *
 * <p>The provider uses state-aware current evidence and only builds the typed candidate input
 * shared by the score endpoint and analysis candidate endpoint. It does not calculate distance or
 * contribution roles.
 */
@Component
@RequiredArgsConstructor
public final class FeedstockBatchProfileTargetScorableCandidateProvider {

  private final @NonNull FeedstockPlanningCandidateProvider candidateProvider;
  private final @NonNull FeedstockPlanningGraphAnalyzer graphAnalyzer;

  @NonNull List<@NonNull FeedstockBatchProfileTargetScorableCandidate> findCandidates(
      @NonNull FeedstockBatchProfileTargetQuery query
  ) {
    requireNonNull(query, "feedstock batch profile target query cannot be null");

    return ChunkedCursorPageCollector.collect(
        CursorPageRequest.firstPage(query.limit()),
        (afterPosition, limit) -> candidateProvider.findCandidates(
            new FeedstockPlanningCandidateQuery(
                query.workspaceId(),
                query.filters().batch(),
                query.filters().season(),
                afterPosition,
                limit
            )
        ),
        FeedstockBatchProfileTargetScorableCandidateProvider::toPagePosition,
        candidate -> toCandidateIfScorable(candidate.currentProfile(), query)
    ).items();
  }

  private @NonNull Optional<@NonNull FeedstockBatchProfileTargetScorableCandidate> toCandidateIfScorable(
      @NonNull FeedstockCurrentProfileView currentProfile,
      @NonNull FeedstockBatchProfileTargetQuery query
  ) {
    requireNonNull(currentProfile, "feedstock current profile cannot be null");
    final var graphEvaluation = graphAnalyzer.evaluate(
        currentProfile.specificGravity(),
        currentProfile.titratableAcidity()
    );
    if (!graphEvaluation.plottable()) {
      return Optional.empty();
    }
    final var sourceZoneCriteria = query.sourceZoneCriteria();
    if (sourceZoneCriteria != null
        && !sourceZoneCriteria.matches(graphEvaluation)) {
      return Optional.empty();
    }
    final var specificGravity = currentProfile.specificGravity();
    final var titratableAcidity = currentProfile.titratableAcidity();
    if (specificGravity == null || titratableAcidity == null) {
      return Optional.empty();
    }

    return Optional.of(new FeedstockBatchProfileTargetScorableCandidate(
        currentProfile,
        new FeedstockTargetFitAnalyzer.Candidate(
            referenceId(currentProfile),
            new FeedstockTargetCharacteristics(
                specificGravity,
                titratableAcidity,
                currentProfile.tannin()
            )
        )
    ));
  }

  private static @NonNull String referenceId(
      @NonNull FeedstockCurrentProfileView currentProfile
  ) {
    requireNonNull(currentProfile, "feedstock current profile cannot be null");
    return currentProfile.batchId().value().toString();
  }

  private static @NonNull FeedstockPlanningCandidatePagePosition toPagePosition(
      @NonNull FeedstockPlanningCandidateSnapshot candidate
  ) {
    requireNonNull(candidate, "feedstock planning candidate cannot be null");
    final var latestMeasurement = candidate.currentProfile().latestMeasurementSet();
    return new FeedstockPlanningCandidatePagePosition(
        latestMeasurement.measuredAt(),
        latestMeasurement.measurementSetId()
    );
  }
}
