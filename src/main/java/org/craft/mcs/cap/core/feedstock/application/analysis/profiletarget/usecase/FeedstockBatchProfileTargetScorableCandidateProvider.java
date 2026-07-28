package org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.usecase;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidatePagePosition;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidate;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidateReference;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.compatibility.ChemicalProfileTargetScoringInputAdapter;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentAdapter;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentMatcher;
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
  private final @NonNull ChemicalProfileAssessmentAdapter chemicalProfileAssessmentAdapter;
  private final @NonNull ChemicalProfileAssessmentMatcher chemicalProfileAssessmentMatcher;

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
    final var evaluation = chemicalProfileAssessmentAdapter.project(currentProfile.chemicalProfile());
    if (!evaluation.plottable()) {
      return Optional.empty();
    }
    if (!chemicalProfileAssessmentMatcher.matches(
        evaluation,
        query.filters().capability(),
        query.filters().sugarAcidityZone()
    )) {
      return Optional.empty();
    }

    final var assessment = evaluation.assessment();
    if (assessment == null) {
      return Optional.empty();
    }
    final var input = ChemicalProfileTargetScoringInputAdapter.adapt(currentProfile.chemicalProfile())
        .orElseThrow();

    return Optional.of(new FeedstockBatchProfileTargetScorableCandidate(
        currentProfile,
        new ProfileTargetDistanceCandidate(
            referenceId(currentProfile),
            input
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
    final var latestMeasurement = currentProfile.latestMeasurement();
    return new FeedstockAnalysisCandidatePagePosition(
        latestMeasurement.measuredAt(),
        latestMeasurement.measurementId().value()
    );
  }
}
