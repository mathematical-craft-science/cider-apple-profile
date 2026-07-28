package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.mapper;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidatePagePosition;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessment;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidateFilters;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZoneResult;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileCapabilityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.ProfileGraphRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockMeasurementSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockMeasurementSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockMeasurementSugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.UnplottableFeedstockMeasurementSugarAcidityZonePointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.cursor.FeedstockSugarAcidityCursorAdapter;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphAssessmentResponse;
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
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.ProfileGraphCriteriaRestMapper
    .toCapabilityCriteria;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.ProfileGraphCriteriaRestMapper
    .toZoneCriteria;


@Component
@RequiredArgsConstructor
public final class FeedstockMeasurementProfileGraphRestMapper {

  private final @NonNull FeedstockSugarAcidityCursorAdapter cursorAdapter;

  public @NonNull FeedstockMeasurementSugarAcidityZonePointQuery toQuery(
      @NonNull FeedstockMeasurementSugarAcidityZonePointQueryRequest request
  ) {
    requireNonNull(request, "feedstock measurement sugar-acidity zone point query request cannot be null");
    final var workspaceId = WorkspaceId.of(request.workspaceId());
    final var filters = new FeedstockAnalysisCandidateFilters(
        toBatchCriteria(request.batchId()),
        toSeason(request.season()),
        toZoneCriteria(request.zone()),
        toCapabilityCriteria(request.capability(), request.capable())
    );

    return new FeedstockMeasurementSugarAcidityZonePointQuery(
        workspaceId,
        filters,
        new CursorPageRequest<>(
            cursorAdapter.decodeHistorical(request.after(), workspaceId, filters),
            requireNonNullElse(request.limit(), QueryLimits.DEFAULT)
        )
    );
  }

  public static @NonNull FeedstockMeasurementSugarAcidityZoneResponse toResponse(
      @NonNull FeedstockMeasurementSugarAcidityZoneResult result
  ) {
    requireNonNull(result, "feedstock measurement sugar-acidity zone result cannot be null");
    final var evaluation = result.evaluation();

    return new FeedstockMeasurementSugarAcidityZoneResponse(
        result.measurementId().value(),
        result.workspaceId().value(),
        result.batchId().value(),
        result.measurementName().value(),
        evaluation.plottable(),
        toAssessmentResponse(evaluation),
        ChemicalProfileCapabilityRestMapper.toResponse(evaluation.capabilityAssessment())
    );
  }

  public @NonNull FeedstockMeasurementSugarAcidityZonePointBatchResponse toResponse(
      @NonNull CursorPage<
          @NonNull FeedstockMeasurementSugarAcidityZoneResult,
          @NonNull FeedstockAnalysisCandidatePagePosition
      > page,
      @NonNull FeedstockMeasurementSugarAcidityZonePointQuery query
  ) {
    requireNonNull(page, "feedstock measurement sugar-acidity zone point page cannot be null");
    requireNonNull(query, "feedstock measurement sugar-acidity zone point query cannot be null");

    return new FeedstockMeasurementSugarAcidityZonePointBatchResponse(
        page.items().stream()
            .filter(result -> result.evaluation().plottable())
            .map(FeedstockMeasurementProfileGraphRestMapper::toResponse)
            .toList(),
        page.items().stream()
            .filter(result -> !result.evaluation().plottable())
            .map(FeedstockMeasurementProfileGraphRestMapper::toUnplottableResponse)
            .toList(),
        cursorAdapter.encodeHistorical(page.nextPosition(), query.workspaceId(), query.filters())
    );
  }

  private static @NonNull UnplottableFeedstockMeasurementSugarAcidityZonePointResponse toUnplottableResponse(
      @NonNull FeedstockMeasurementSugarAcidityZoneResult result
  ) {
    requireNonNull(result, "feedstock measurement sugar-acidity zone result cannot be null");

    return new UnplottableFeedstockMeasurementSugarAcidityZonePointResponse(
        result.measurementId().value(),
        result.workspaceId().value(),
        result.batchId().value(),
        result.measurementName().value(),
        ChemicalProfileCapabilityRestMapper.toResponse(result.evaluation().capabilityAssessment())
    );
  }

  private static @Nullable SugarAcidityGraphAssessmentResponse toAssessmentResponse(
      @NonNull ChemicalProfileAssessment evaluation
  ) {
    return evaluation.assessment() == null ? null : ProfileGraphRestMapper.toResponse(evaluation);
  }
}
