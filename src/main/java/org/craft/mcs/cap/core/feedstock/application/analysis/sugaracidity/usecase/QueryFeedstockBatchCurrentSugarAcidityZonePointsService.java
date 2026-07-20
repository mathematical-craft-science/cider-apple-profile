package org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.usecase;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.model.FeedstockMeasurementSugarAcidityPagePosition;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchSummaryQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.LatestFeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.application.query.pagination.ChunkedCursorPageCollector;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPage;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.application.model.SugarAcidityZoneCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;


/**
 * Read-side use case for current feedstock batch points on the sugar-acidity graph.
 *
 * <p>The service applies the product rule that only the latest measurement of each batch represents
 * the current batch profile. It then evaluates capability and zone filters in the application
 * layer, returning cursor pages for feedstock graph clients.
 */
@Component
@RequiredArgsConstructor
public final class QueryFeedstockBatchCurrentSugarAcidityZonePointsService
    implements QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase {

  private final @NonNull FeedstockBatchSummaryQueryRepository feedstockBatchQueryRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull FeedstockBatchCurrentMeasurementQueryService currentMeasurementQueryService;
  private final @NonNull SugarAcidityProfileProjector sugarAcidityProfileProjector;

  @Override
  public @NonNull CursorPage<
      @NonNull FeedstockBatchCurrentSugarAcidityZoneResult,
      @NonNull FeedstockMeasurementSugarAcidityPagePosition
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
        (afterPosition, limit) -> currentMeasurementQueryService.findCurrentSugarAcidityPointCandidates(
            new LatestFeedstockMeasurementSugarAcidityPointCandidateQuery(
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
      @NonNull FeedstockMeasurementView measurement,
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery query,
      @NonNull FeedstockBatchSummary batch,
      @NonNull SugarAcidityProfileEvaluation evaluation
  ) {
    return new FeedstockBatchCurrentSugarAcidityZoneResult(
        measurement.batchId(),
        batch.name(),
        query.workspaceId(),
        measurement.measurementId(),
        measurement.measuredAt(),
        evaluation
    );
  }

  private @NonNull Optional<@NonNull FeedstockBatchCurrentSugarAcidityZoneResult> toResultIfMatches(
      @NonNull FeedstockMeasurementView measurement,
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery query,
      @NonNull Map<@NonNull FeedstockBatchId, @NonNull FeedstockBatchSummary> batchesById
  ) {
    final var batch = batchesById.get(measurement.batchId());
    if (batch == null) {
      return Optional.empty();
    }

    final var evaluation = sugarAcidityProfileProjector.project(measurement.chemicalProfile());
    if (!matchesCapability(evaluation, query.filters().capability())) {
      return Optional.empty();
    }

    final var result = toResult(measurement, query, batch, evaluation);
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
      @NonNull FeedstockBatchCurrentSugarAcidityZoneResult result,
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
      @NonNull FeedstockMeasurementView measurement
  ) {
    requireNonNull(measurement, "feedstock measurement cannot be null");
    return new FeedstockMeasurementSugarAcidityPagePosition(
        measurement.measuredAt(),
        measurement.measurementId().value()
    );
  }
}
