package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.mapper;

import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.query.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileCapabilityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchCurrentSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchCurrentSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockBatchGraphPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityZoneAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.platform.query.CursorPage;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;


public final class FeedstockBatchSugarAcidityRestMapper {

  private FeedstockBatchSugarAcidityRestMapper() {
  }

  public static @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery toQuery(
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQueryRequest request
  ) {
    requireNonNull(request, "feedstock batch current sugar-acidity zone point query request cannot be null");

    return new FeedstockBatchCurrentSugarAcidityZonePointQuery(
        CiderMakerId.of(request.ciderMakerId()),
        new FeedstockMeasurementPointFilters(
            toBatchCriteria(request.batchId()),
            toSeason(request.season()),
            toZoneCriteria(request.zone()),
            toCapabilityCriteria(request.capability(), request.capable())
        ),
        new CursorPageRequest(
            request.after(),
            requireNonNullElse(request.limit(), CursorPageRequest.DEFAULT_LIMIT)
        )
    );
  }

  public static @NonNull FeedstockBatchGraphPointResponse toResponse(
      @NonNull FeedstockBatchCurrentSugarAcidityZoneResult result
  ) {
    requireNonNull(result, "feedstock batch current sugar-acidity zone result cannot be null");

    final var assessment = result.assessment();
    return new FeedstockBatchGraphPointResponse(
        result.batchId().value(),
        result.batchName().value(),
        result.ciderMakerId().value(),
        result.currentMeasurementId().value(),
        result.measuredAt(),
        result.plottable(),
        toPointResponse(assessment),
        toZoneResponse(assessment),
        missingAxes(result.capabilityAssessment()),
        toAssessmentResponse(assessment),
        ChemicalProfileCapabilityRestMapper.toResponse(result.capabilityAssessment())
    );
  }

  public static @NonNull FeedstockBatchCurrentSugarAcidityZonePointBatchResponse toResponse(
      @NonNull CursorPage<@NonNull FeedstockBatchCurrentSugarAcidityZoneResult> page
  ) {
    requireNonNull(page, "feedstock batch current sugar-acidity zone point page cannot be null");

    return new FeedstockBatchCurrentSugarAcidityZonePointBatchResponse(
        page.items().stream()
            .map(FeedstockBatchSugarAcidityRestMapper::toResponse)
            .toList(),
        page.nextCursor()
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
