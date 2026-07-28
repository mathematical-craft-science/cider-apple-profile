package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.mapper;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidatePagePosition;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessment;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidateFilters;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchCurrentSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchCurrentSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchGraphPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.cursor.FeedstockSugarAcidityCursorAdapter;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.ProfileGraphRestMapper;
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
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.ProfileGraphCriteriaRestMapper.toCapabilityCriteria;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.ProfileGraphCriteriaRestMapper.toZoneCriteria;


@Component
@RequiredArgsConstructor
public final class FeedstockBatchSugarAcidityRestMapper {

  private final @NonNull FeedstockSugarAcidityCursorAdapter cursorAdapter;

  public @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery toQuery(
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQueryRequest request
  ) {
    requireNonNull(request, "feedstock batch current sugar-acidity zone point query request cannot be null");
    final var workspaceId = WorkspaceId.of(request.workspaceId());
    final var filters = new FeedstockAnalysisCandidateFilters(
        toBatchCriteria(request.batchId()),
        toSeason(request.season()),
        toZoneCriteria(request.zone()),
        toCapabilityCriteria(request.capability(), request.capable())
    );

    return new FeedstockBatchCurrentSugarAcidityZonePointQuery(
        workspaceId,
        filters,
        new CursorPageRequest<>(
            cursorAdapter.decodeCurrent(request.after(), workspaceId, filters),
            requireNonNullElse(request.limit(), QueryLimits.DEFAULT)
        )
    );
  }

  public static @NonNull FeedstockBatchGraphPointResponse toResponse(
      @NonNull FeedstockBatchCurrentSugarAcidityZoneResult result
  ) {
    requireNonNull(result, "feedstock batch current sugar-acidity zone result cannot be null");

    final var evaluation = result.evaluation();
    final var assessment = evaluation.assessment();
    return new FeedstockBatchGraphPointResponse(
        result.batchId().value(),
        result.batchName().value(),
        result.workspaceId().value(),
        result.currentMaterialStateId().value(),
        result.currentMeasurementId().value(),
        result.measuredAt(),
        evaluation.plottable(),
        toPointResponse(evaluation),
        toZoneResponse(assessment),
        evaluation.northAmericanTanninLevel(),
        evaluation.graphEvaluation().missingCharacteristics().stream().toList()
    );
  }

  public @NonNull FeedstockBatchCurrentSugarAcidityZonePointBatchResponse toResponse(
      @NonNull CursorPage<
          @NonNull FeedstockBatchCurrentSugarAcidityZoneResult,
          @NonNull FeedstockAnalysisCandidatePagePosition
      > page,
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery query
  ) {
    requireNonNull(page, "feedstock batch current sugar-acidity zone point page cannot be null");
    requireNonNull(query, "feedstock batch current sugar-acidity zone point query cannot be null");

    return new FeedstockBatchCurrentSugarAcidityZonePointBatchResponse(
        page.items().stream()
            .map(FeedstockBatchSugarAcidityRestMapper::toResponse)
            .toList(),
        cursorAdapter.encodeCurrent(page.nextPosition(), query.workspaceId(), query.filters())
    );
  }

  private static @Nullable SugarAcidityGraphPointResponse toPointResponse(
      @NonNull ChemicalProfileAssessment evaluation
  ) {
    return evaluation.plottable() ? ProfileGraphRestMapper.toResponse(evaluation.axes()) : null;
  }

  private static @Nullable JolicoeurSugarAcidityZone toZoneResponse(
      @Nullable JolicoeurSugarAcidityZoneAssessment assessment
  ) {
    return assessment == null ? null : assessment.zone();
  }

}
