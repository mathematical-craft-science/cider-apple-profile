package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.mapper;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.model.FeedstockMeasurementSugarAcidityPagePosition;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.application.model.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZoneResult;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileCapabilityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockMeasurementSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockMeasurementSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockMeasurementSugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.UnplottableFeedstockMeasurementSugarAcidityZonePointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.cursor.FeedstockSugarAcidityCursorAdapter;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityZoneAssessmentResponse;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPage;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;


@Component
@RequiredArgsConstructor
public final class FeedstockMeasurementSugarAcidityZoneRestMapper {

  private final @NonNull FeedstockSugarAcidityCursorAdapter cursorAdapter;

  public static @NonNull FeedstockMeasurementSugarAcidityZoneResponse toResponse(
      @NonNull FeedstockMeasurementSugarAcidityZoneAssessment assessment
  ) {
    requireNonNull(assessment, "feedstock measurement sugar-acidity zone assessment cannot be null");

    return new FeedstockMeasurementSugarAcidityZoneResponse(
        assessment.measurementId().value(),
        assessment.workspaceId().value(),
        assessment.batchId().value(),
        assessment.measurementName().value(),
        SugarAcidityZoneRestMapper.toResponse(assessment.assessment())
    );
  }

  public @NonNull FeedstockMeasurementSugarAcidityZonePointQuery toQuery(
      @NonNull FeedstockMeasurementSugarAcidityZonePointQueryRequest request
  ) {
    requireNonNull(request, "feedstock measurement sugar-acidity zone point query request cannot be null");
    final var workspaceId = WorkspaceId.of(request.workspaceId());
    final var filters = new FeedstockMeasurementPointFilters(
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
        toAssessmentResponse(evaluation.assessment()),
        ChemicalProfileCapabilityRestMapper.toResponse(evaluation.capabilityAssessment())
    );
  }

  public @NonNull FeedstockMeasurementSugarAcidityZonePointBatchResponse toResponse(
      @NonNull CursorPage<
          @NonNull FeedstockMeasurementSugarAcidityZoneResult,
          @NonNull FeedstockMeasurementSugarAcidityPagePosition
      > page,
      @NonNull FeedstockMeasurementSugarAcidityZonePointQuery query
  ) {
    requireNonNull(page, "feedstock measurement sugar-acidity zone point page cannot be null");
    requireNonNull(query, "feedstock measurement sugar-acidity zone point query cannot be null");

    return new FeedstockMeasurementSugarAcidityZonePointBatchResponse(
        page.items().stream()
            .filter(result -> result.evaluation().plottable())
            .map(FeedstockMeasurementSugarAcidityZoneRestMapper::toResponse)
            .toList(),
        page.items().stream()
            .filter(result -> !result.evaluation().plottable())
            .map(FeedstockMeasurementSugarAcidityZoneRestMapper::toUnplottableResponse)
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

  private static @Nullable FeedstockSeason toSeason(@Nullable FeedstockSeasonRequest request) {
    return request == null ? null : FeedstockSeason.of(request.startsOn(), request.endsOn());
  }

  private static @Nullable FeedstockBatchCriteria toBatchCriteria(@Nullable UUID batchId) {
    return batchId == null ? null : FeedstockBatchCriteria.of(FeedstockBatchId.of(batchId));
  }

  private static @Nullable SugarAcidityZoneCriteria toZoneCriteria(@Nullable SugarAcidityZone zone) {
    return zone == null ? null : SugarAcidityZoneCriteria.of(zone);
  }

  private static @Nullable ChemicalProfileCapabilityCriteria toCapabilityCriteria(
      @Nullable ChemicalProfileCapability capability,
      @Nullable Boolean capable
  ) {
    return capability == null ? null : new ChemicalProfileCapabilityCriteria(capability, capable);
  }

  private static @Nullable SugarAcidityZoneAssessmentResponse toAssessmentResponse(
      @Nullable SugarAcidityZoneAssessment assessment
  ) {
    return assessment == null ? null : SugarAcidityZoneRestMapper.toResponse(assessment);
  }
}
