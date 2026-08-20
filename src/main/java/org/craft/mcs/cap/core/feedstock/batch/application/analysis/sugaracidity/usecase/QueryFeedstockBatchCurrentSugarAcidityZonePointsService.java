package org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.usecase;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePagePosition;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.classification.port.out.FeedstockTanninLevelAnalyzer;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.contract.FeedstockSugarAcidityGraphView;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.port.out.FeedstockSugarAcidityGraphAnalyzer;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.port.in.QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchSummaryQueryRepository;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePageQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.out.FeedstockCurrentProfilePageProvider;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.query.pagination.ChunkedCursorPageCollector;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPage;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic.SPECIFIC_GRAVITY;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic.TITRATABLE_ACIDITY;
import static java.util.stream.Collectors.toMap;


/**
 * Read-side use case for current feedstock batch points on the sugar-acidity graph.
 *
 * <p>The service projects each batch's current material state, then evaluates graph
 * filters in the application layer and returns cursor pages for feedstock graph clients.
 */
@Component
@RequiredArgsConstructor
public final class QueryFeedstockBatchCurrentSugarAcidityZonePointsService
    implements QueryFeedstockBatchCurrentSugarAcidityZonePointsUseCase {

  private final @NonNull FeedstockBatchSummaryQueryRepository feedstockBatchQueryRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull FeedstockCurrentProfilePageProvider currentProfileProvider;
  private final @NonNull FeedstockSugarAcidityGraphAnalyzer graphAnalyzer;
  private final @NonNull FeedstockTanninLevelAnalyzer tanninLevelAnalyzer;

  @Override
  public @NonNull CursorPage<
      @NonNull FeedstockBatchCurrentSugarAcidityZoneResult,
      @NonNull FeedstockCurrentProfilePagePosition
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
        (afterPosition, limit) -> currentProfileProvider.findProfiles(
            new FeedstockCurrentProfilePageQuery(
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
      @NonNull FeedstockSugarAcidityGraphView graphEvaluation
  ) {
    final var tannin = currentProfile.tannin();
    return new FeedstockBatchCurrentSugarAcidityZoneResult(
        currentProfile.batchId(),
        batch.name(),
        query.workspaceId(),
        currentProfile.currentMaterialStateId(),
        currentProfile.latestMeasurementSet(),
        currentProfile.evidenceFor(Set.of(SPECIFIC_GRAVITY, TITRATABLE_ACIDITY)),
        currentProfile.specificGravity(),
        currentProfile.titratableAcidity(),
        graphEvaluation,
        graphEvaluation.plottable() && tannin != null
            ? tanninLevelAnalyzer.assess(tannin) // NOSONAR
            : null
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

    final var graphEvaluation = graphAnalyzer.evaluate(
        currentProfile.specificGravity(),
        currentProfile.titratableAcidity()
    );
    final var graphCriteria = query.graphCriteria();
    if (graphCriteria != null && !graphCriteria.matches(graphEvaluation)) {
      return Optional.empty();
    }

    return Optional.of(toResult(currentProfile, query, batch, graphEvaluation));
  }

  private static @NonNull FeedstockCurrentProfilePagePosition toPagePosition(
      @NonNull FeedstockCurrentProfileView currentProfile
  ) {
    requireNonNull(currentProfile, "feedstock current profile cannot be null");
    final var latestMeasurement = currentProfile.latestMeasurementSet();
    return new FeedstockCurrentProfilePagePosition(
        latestMeasurement.measuredAt(),
        latestMeasurement.measurementSetId()
    );
  }
}
