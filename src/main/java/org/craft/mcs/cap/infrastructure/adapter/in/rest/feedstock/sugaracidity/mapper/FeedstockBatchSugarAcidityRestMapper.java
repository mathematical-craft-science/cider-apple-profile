package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.mapper;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePagePosition;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.contract.FeedstockSugarAcidityGraphView;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileFilters;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchCurrentSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchCurrentSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchGraphPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.classification.NorthAmericanTanninLevel;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.cursor.FeedstockSugarAcidityCursorAdapter;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockProfileEvidenceRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.GraphMissingCharacteristic;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.JolicoeurSugarAcidityGraphRestMapper;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPage;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockCriteriaRestMapper.toBatchCriteria;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper.FeedstockCriteriaRestMapper.toSeason;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.JolicoeurSugarAcidityGraphCriteriaRestMapper.toFeedstockGraphCriteria;


@Component
@RequiredArgsConstructor
public final class FeedstockBatchSugarAcidityRestMapper {

  private final @NonNull FeedstockSugarAcidityCursorAdapter cursorAdapter;

  public @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery toQuery(
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQueryRequest request
  ) {
    requireNonNull(request, "feedstock batch current sugar-acidity zone point query request cannot be null");
    final var workspaceId = WorkspaceId.of(request.workspaceId());
    final var filters = new FeedstockCurrentProfileFilters(
        toBatchCriteria(request.batchId()),
        toSeason(request.season())
    );
    final var graphCriteria = toFeedstockGraphCriteria(request.zone(), request.plottable());

    return new FeedstockBatchCurrentSugarAcidityZonePointQuery(
        workspaceId,
        filters,
        graphCriteria,
        new CursorPageRequest<>(
            cursorAdapter.decodeCurrent(request.after(), workspaceId, filters, graphCriteria),
            requireNonNullElse(request.limit(), QueryLimits.DEFAULT)
        )
    );
  }

  public static @NonNull FeedstockBatchGraphPointResponse toResponse(
      @NonNull FeedstockBatchCurrentSugarAcidityZoneResult result
  ) {
    requireNonNull(result, "feedstock batch current sugar-acidity zone result cannot be null");

    final var graphEvaluation = result.graphEvaluation();
    final var northAmericanTanninLevel = result.northAmericanTanninLevel();
    return new FeedstockBatchGraphPointResponse(
        result.batchId().value(),
        result.batchName().value(),
        result.workspaceId().value(),
        result.currentMaterialStateId().value(),
        result.profileEvidence().stream()
            .map(FeedstockProfileEvidenceRestMapper::toResponse)
            .toList(),
        graphEvaluation.plottable(),
        toPointResponse(result),
        toZoneResponse(graphEvaluation.zoneAssessment()),
        northAmericanTanninLevel == null
            ? null
            : NorthAmericanTanninLevel.valueOf(northAmericanTanninLevel.name()),
        graphEvaluation.missingCharacteristics().stream()
            .map(characteristic -> GraphMissingCharacteristic.valueOf(characteristic.name()))
            .toList()
    );
  }

  public @NonNull FeedstockBatchCurrentSugarAcidityZonePointBatchResponse toResponse(
      @NonNull CursorPage<
          @NonNull FeedstockBatchCurrentSugarAcidityZoneResult,
          @NonNull FeedstockCurrentProfilePagePosition
      > page,
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery query
  ) {
    requireNonNull(page, "feedstock batch current sugar-acidity zone point page cannot be null");
    requireNonNull(query, "feedstock batch current sugar-acidity zone point query cannot be null");

    return new FeedstockBatchCurrentSugarAcidityZonePointBatchResponse(
        page.items().stream()
            .map(FeedstockBatchSugarAcidityRestMapper::toResponse)
            .toList(),
        cursorAdapter.encodeCurrent(
            page.nextPosition(),
            query.workspaceId(),
            query.filters(),
            query.graphCriteria()
        )
    );
  }

  private static @Nullable SugarAcidityGraphPointResponse toPointResponse(
      @NonNull FeedstockBatchCurrentSugarAcidityZoneResult result
  ) {
    if (!result.graphEvaluation().plottable()) {
      return null;
    }
    return JolicoeurSugarAcidityGraphRestMapper.toResponse(
        requireNonNull(result.specificGravity()),
        requireNonNull(result.titratableAcidity())
    );
  }

  private static @Nullable JolicoeurSugarAcidityZone toZoneResponse(
      FeedstockSugarAcidityGraphView.@Nullable ZoneAssessment assessment
  ) {
    if (assessment == null) {
      return null;
    }
    final var zone = assessment.zone();
    return zone == null ? null : JolicoeurSugarAcidityZone.valueOf(zone.name());
  }

}
