package org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.usecase;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidatePagePosition;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.service.FeedstockGraphCandidateMatcher;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidate;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateReference;
import org.craft.mcs.cap.core.profileanalysis.graph.application.port.in.EvaluateJolicoeurSugarAcidityGraphUseCase;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringInput;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockBatchProfileTargetQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.CurrentFeedstockBatchAnalysisCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.FeedstockCurrentProfileAnalysisCandidateRepository;
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

  private static final @NonNull String REFERENCE_TYPE = "feedstock-batch";

  private final @NonNull FeedstockCurrentProfileAnalysisCandidateRepository candidateRepository;
  private final @NonNull EvaluateJolicoeurSugarAcidityGraphUseCase graphEvaluator;
  private final @NonNull FeedstockGraphCandidateMatcher graphCandidateMatcher;

  @NonNull List<@NonNull FeedstockBatchProfileTargetScorableCandidate> findCandidates(
      @NonNull FeedstockBatchProfileTargetQuery query
  ) {
    requireNonNull(query, "feedstock batch profile target query cannot be null");

    return ChunkedCursorPageCollector.collect(
        CursorPageRequest.firstPage(query.limit()),
        (afterPosition, limit) -> candidateRepository.findCandidates(
            new CurrentFeedstockBatchAnalysisCandidateQuery(
                query.workspaceId(),
                query.filters().batch(),
                query.filters().season(),
                afterPosition,
                limit
            )
        ),
        FeedstockBatchProfileTargetScorableCandidateProvider::toPagePosition,
        measurement -> toCandidateIfScorable(measurement, query)
    ).items();
  }

  private @NonNull Optional<@NonNull FeedstockBatchProfileTargetScorableCandidate> toCandidateIfScorable(
      @NonNull FeedstockCurrentProfileView currentProfile,
      @NonNull FeedstockBatchProfileTargetQuery query
  ) {
    requireNonNull(currentProfile, "feedstock current profile cannot be null");
    final var graphEvaluation = graphEvaluator.evaluate(
        currentProfile.specificGravity(),
        currentProfile.titratableAcidity()
    );
    if (!graphEvaluation.plottable()) {
      return Optional.empty();
    }
    if (!graphCandidateMatcher.matches(currentProfile, graphEvaluation, query.filters())) {
      return Optional.empty();
    }
    final var specificGravity = currentProfile.specificGravity();
    final var titratableAcidity = currentProfile.titratableAcidity();
    if (specificGravity == null || titratableAcidity == null) {
      return Optional.empty();
    }

    return Optional.of(new FeedstockBatchProfileTargetScorableCandidate(
        currentProfile,
        new ProfileTargetDistanceCandidate(
            referenceId(currentProfile),
            new ProfileTargetScoringInput(
                specificGravity,
                titratableAcidity,
                currentProfile.tannin()
            )
        )
    ));
  }

  private static @NonNull ProfileTargetDistanceCandidateReference referenceId(
      @NonNull FeedstockCurrentProfileView currentProfile
  ) {
    requireNonNull(currentProfile, "feedstock current profile cannot be null");
    return new ProfileTargetDistanceCandidateReference(
        REFERENCE_TYPE,
        currentProfile.batchId().value().toString()
    );
  }

  private static @NonNull FeedstockAnalysisCandidatePagePosition toPagePosition(
      @NonNull FeedstockCurrentProfileView currentProfile
  ) {
    requireNonNull(currentProfile, "feedstock current profile cannot be null");
    final var latestMeasurement = currentProfile.latestMeasurementSet();
    return new FeedstockAnalysisCandidatePagePosition(
        latestMeasurement.measuredAt(),
        latestMeasurement.measurementSetId()
    );
  }
}
