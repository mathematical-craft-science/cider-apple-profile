package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.mapper;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.model.FeedstockMeasurementSugarAcidityPagePosition;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.application.model.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileCapabilityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchCurrentSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchCurrentSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchGraphPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.cursor.FeedstockSugarAcidityCursorAdapter;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityZoneAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
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
public final class FeedstockBatchSugarAcidityRestMapper {

  private final @NonNull FeedstockSugarAcidityCursorAdapter cursorAdapter;

  public @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery toQuery(
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQueryRequest request
  ) {
    requireNonNull(request, "feedstock batch current sugar-acidity zone point query request cannot be null");
    final var workspaceId = WorkspaceId.of(request.workspaceId());
    final var filters = new FeedstockMeasurementPointFilters(
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
        result.currentMeasurementId().value(),
        result.measuredAt(),
        evaluation.plottable(),
        toPointResponse(assessment),
        toZoneResponse(assessment),
        missingAxes(evaluation.capabilityAssessment()),
        toAssessmentResponse(assessment),
        ChemicalProfileCapabilityRestMapper.toResponse(evaluation.capabilityAssessment())
    );
  }

  public @NonNull FeedstockBatchCurrentSugarAcidityZonePointBatchResponse toResponse(
      @NonNull CursorPage<
          @NonNull FeedstockBatchCurrentSugarAcidityZoneResult,
          @NonNull FeedstockMeasurementSugarAcidityPagePosition
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

  private static @NonNull List<@NonNull ChemicalAxis> missingAxes(
      @NonNull ChemicalProfileCapabilityAssessment assessment
  ) {
    return assessment.missingAxes().stream().toList();
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

  private static @Nullable SugarAcidityProfilePointResponse toPointResponse(
      @Nullable SugarAcidityZoneAssessment assessment
  ) {
    return assessment == null ? null : SugarAcidityZoneRestMapper.toResponse(assessment.point());
  }

  private static @Nullable SugarAcidityZone toZoneResponse(
      @Nullable SugarAcidityZoneAssessment assessment
  ) {
    return assessment == null ? null : assessment.zone();
  }

  private static @Nullable SugarAcidityZoneAssessmentResponse toAssessmentResponse(
      @Nullable SugarAcidityZoneAssessment assessment
  ) {
    return assessment == null ? null : SugarAcidityZoneRestMapper.toResponse(assessment);
  }
}
