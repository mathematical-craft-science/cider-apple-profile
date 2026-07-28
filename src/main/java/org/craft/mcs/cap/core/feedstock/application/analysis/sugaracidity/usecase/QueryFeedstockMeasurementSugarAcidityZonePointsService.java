package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.usecase;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidatePagePosition;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessment;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentAdapter;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentMatcher;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.QueryFeedstockMeasurementSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.FeedstockMeasurementAnalysisCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.FeedstockMeasurementAnalysisCandidateRepository;
import org.craft.mcs.cap.shared.application.query.pagination.ChunkedCursorPageCollector;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPage;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Read-side use case for historical feedstock measurement points on the sugar-acidity graph.
 *
 * <p>This query includes all matching measurements, not only the latest per batch. It is useful for
 * audit/history views and for understanding how a batch profile evolved over time. Current
 * decision-support flows should use {@link QueryFeedstockBatchCurrentSugarAcidityZonePointsService}
 * instead.
 */
@Component
@RequiredArgsConstructor
public final class QueryFeedstockMeasurementSugarAcidityZonePointsService
    implements QueryFeedstockMeasurementSugarAcidityZonePointsUseCase {

  private final @NonNull FeedstockMeasurementAnalysisCandidateRepository candidateRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull ChemicalProfileAssessmentAdapter chemicalProfileAssessmentAdapter;
  private final @NonNull ChemicalProfileAssessmentMatcher chemicalProfileAssessmentMatcher;

  @Override
  public @NonNull CursorPage<
      @NonNull FeedstockMeasurementSugarAcidityZoneResult,
      @NonNull FeedstockAnalysisCandidatePagePosition
  > query(
      @NonNull FeedstockMeasurementSugarAcidityZonePointQuery query
  ) {
    requireNonNull(query, "feedstock measurement sugar-acidity zone point query cannot be null");
    feedstockAccessControl.requireReadAccess(query.workspaceId());

    return ChunkedCursorPageCollector.collect(
        query.page(),
        (afterPosition, limit) -> candidateRepository.findCandidates(
            new FeedstockMeasurementAnalysisCandidateQuery(
                query.workspaceId(),
                query.filters().batch(),
                query.filters().season(),
                afterPosition,
                limit
            )
        ),
        QueryFeedstockMeasurementSugarAcidityZonePointsService::toPagePosition,
        sample -> toResultIfMatches(sample, query)
    );
  }

  private @NonNull FeedstockMeasurementSugarAcidityZoneResult toResult(
      @NonNull FeedstockMeasurementView sample,
      @NonNull FeedstockMeasurementSugarAcidityZonePointQuery query,
      @NonNull ChemicalProfileAssessment evaluation
  ) {
    return new FeedstockMeasurementSugarAcidityZoneResult(
        sample.measurementId(),
        query.workspaceId(),
        sample.batchId(),
        sample.name(),
        evaluation
    );
  }

  private @NonNull Optional<@NonNull FeedstockMeasurementSugarAcidityZoneResult> toResultIfMatches(
      @NonNull FeedstockMeasurementView sample,
      @NonNull FeedstockMeasurementSugarAcidityZonePointQuery query
  ) {
    final var evaluation = chemicalProfileAssessmentAdapter.project(sample.chemicalProfile());
    if (!chemicalProfileAssessmentMatcher.matches(
        evaluation,
        query.filters().capability(),
        query.filters().sugarAcidityZone()
    )) {
      return Optional.empty();
    }

    return Optional.of(toResult(sample, query, evaluation));
  }

  private static @NonNull FeedstockAnalysisCandidatePagePosition toPagePosition(
      @NonNull FeedstockMeasurementView sample
  ) {
    requireNonNull(sample, "feedstock measurement cannot be null");
    return new FeedstockAnalysisCandidatePagePosition(
        sample.measuredAt(),
        sample.measurementId().value()
    );
  }
}
