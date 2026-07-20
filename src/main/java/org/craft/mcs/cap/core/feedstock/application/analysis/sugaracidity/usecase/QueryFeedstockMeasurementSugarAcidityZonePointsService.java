package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.usecase;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.model.FeedstockMeasurementSugarAcidityPagePosition;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.QueryFeedstockMeasurementSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.FeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.application.query.pagination.ChunkedCursorPageCollector;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPage;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.application.model.SugarAcidityZoneCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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

  private final @NonNull FeedstockMeasurementQueryRepository feedstockMeasurementQueryRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull SugarAcidityProfileProjector sugarAcidityProfileProjector;

  @Override
  public @NonNull CursorPage<
      @NonNull FeedstockMeasurementSugarAcidityZoneResult,
      @NonNull FeedstockMeasurementSugarAcidityPagePosition
  > query(
      @NonNull FeedstockMeasurementSugarAcidityZonePointQuery query
  ) {
    requireNonNull(query, "feedstock measurement sugar-acidity zone point query cannot be null");
    feedstockAccessControl.requireReadAccess(query.workspaceId());

    return ChunkedCursorPageCollector.collect(
        query.page(),
        (afterPosition, limit) -> feedstockMeasurementQueryRepository.findSugarAcidityPointCandidates(
            new FeedstockMeasurementSugarAcidityPointCandidateQuery(
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
      @NonNull SugarAcidityProfileEvaluation evaluation
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
    final var evaluation = sugarAcidityProfileProjector.project(sample.chemicalProfile());
    if (!matchesCapability(evaluation, query.filters().capability())) {
      return Optional.empty();
    }

    final var result = toResult(sample, query, evaluation);
    return matchesSugarAcidityZone(result, query.filters().sugarAcidityZone())
        ? Optional.of(result)
        : Optional.empty();
  }

  private boolean matchesCapability(
      @NonNull SugarAcidityProfileEvaluation evaluation,
      @Nullable ChemicalProfileCapabilityCriteria criteria
  ) {
    if (criteria == null || criteria.capable() == null) {
      return true;
    }

    final var assessment = sugarAcidityProfileProjector.assessCapability(evaluation, criteria.capability());

    return assessment.capable() == criteria.capable(); // NOSONAR
  }

  private static boolean matchesSugarAcidityZone(
      @NonNull FeedstockMeasurementSugarAcidityZoneResult result,
      @Nullable SugarAcidityZoneCriteria criteria
  ) {
    if (criteria == null) {
      return true;
    }

    final SugarAcidityZoneAssessment assessment = result.evaluation().assessment();
    return assessment != null
        && criteria.zones().contains(assessment.zone());
  }

  private static @NonNull FeedstockMeasurementSugarAcidityPagePosition toPagePosition(
      @NonNull FeedstockMeasurementView sample
  ) {
    requireNonNull(sample, "feedstock measurement cannot be null");
    return new FeedstockMeasurementSugarAcidityPagePosition(
        sample.measuredAt(),
        sample.measurementId().value()
    );
  }
}
