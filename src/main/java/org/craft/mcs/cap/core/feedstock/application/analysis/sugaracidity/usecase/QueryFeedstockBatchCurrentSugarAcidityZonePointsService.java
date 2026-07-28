package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.usecase;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidatePagePosition;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessment;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentAdapter;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentMatcher;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchSummaryQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.CurrentFeedstockBatchAnalysisCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.FeedstockCurrentProfileAnalysisCandidateRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.query.pagination.ChunkedCursorPageCollector;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPage;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;


/**
 * Read-side use case for current feedstock batch points on the sugar-acidity graph.
 *
 * <p>The service projects each batch's current material state, then evaluates capability and zone
 * filters in the application layer and returns cursor pages for feedstock graph clients.
 */
@Component
@RequiredArgsConstructor
public final class QueryFeedstockBatchCurrentSugarAcidityZonePointsService
    implements QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase {

  private final @NonNull FeedstockBatchSummaryQueryRepository feedstockBatchQueryRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull FeedstockCurrentProfileAnalysisCandidateRepository candidateRepository;
  private final @NonNull ChemicalProfileAssessmentAdapter chemicalProfileAssessmentAdapter;
  private final @NonNull ChemicalProfileAssessmentMatcher chemicalProfileAssessmentMatcher;

  @Override
  public @NonNull CursorPage<
      @NonNull FeedstockBatchCurrentSugarAcidityZoneResult,
      @NonNull FeedstockAnalysisCandidatePagePosition
  > query(
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery query
  ) {
    requireNonNull(query, "feedstock batch current sugar-acidity zone point query cannot be null");
    feedstockAccessControl.requireReadAccess(query.workspaceId());
    final var batchesById = feedstockBatchQueryRepository.findAllByWorkspaceId(query.workspaceId())
        .stream()
        .collect(toMap(FeedstockBatchSummary::batchId, Function.identity()));

    return ChunkedCursorPageCollector.collect(
        query.page(),
        (afterPosition, limit) -> candidateRepository.findCandidates(
            new CurrentFeedstockBatchAnalysisCandidateQuery(
                query.workspaceId(),
                query.filters().batch(),
                query.filters().season(),
                afterPosition,
                limit
            )
        ),
        QueryFeedstockBatchCurrentSugarAcidityZonePointsService::toPagePosition,
        measurement -> toResultIfMatches(measurement, query, batchesById)
    );
  }

  private @NonNull FeedstockBatchCurrentSugarAcidityZoneResult toResult(
      @NonNull FeedstockCurrentProfileView currentProfile,
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery query,
      @NonNull FeedstockBatchSummary batch,
      @NonNull ChemicalProfileAssessment evaluation
  ) {
    final var latestMeasurement = currentProfile.latestMeasurement();
    return new FeedstockBatchCurrentSugarAcidityZoneResult(
        currentProfile.batchId(),
        batch.name(),
        query.workspaceId(),
        currentProfile.currentMaterialStateId(),
        latestMeasurement.measurementId(),
        latestMeasurement.measuredAt(),
        evaluation
    );
  }

  private @NonNull Optional<@NonNull FeedstockBatchCurrentSugarAcidityZoneResult> toResultIfMatches(
      @NonNull FeedstockCurrentProfileView currentProfile,
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery query,
      @NonNull Map<@NonNull FeedstockBatchId, @NonNull FeedstockBatchSummary> batchesById
  ) {
    final var batch = batchesById.get(currentProfile.batchId());
    if (batch == null) {
      return Optional.empty();
    }

    final var evaluation = chemicalProfileAssessmentAdapter.project(currentProfile.chemicalProfile());
    if (!chemicalProfileAssessmentMatcher.matches(
        evaluation,
        query.filters().capability(),
        query.filters().sugarAcidityZone()
    )) {
      return Optional.empty();
    }

    return Optional.of(toResult(currentProfile, query, batch, evaluation));
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
