package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.mapper;

import java.util.UUID;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.query.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileCapabilityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockMeasurementSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockMeasurementSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.FeedstockMeasurementSugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto.UnplottableFeedstockMeasurementSugarAcidityZonePointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityZoneAssessmentResponse;
import org.craft.mcs.cap.shared.platform.query.CursorPage;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;


public final class FeedstockMeasurementSugarAcidityZoneRestMapper {

  private FeedstockMeasurementSugarAcidityZoneRestMapper() {
  }

  public static @NonNull FeedstockMeasurementSugarAcidityZoneResponse toResponse(
      @NonNull FeedstockMeasurementSugarAcidityZoneAssessment assessment
  ) {
    requireNonNull(assessment, "feedstock measurement sugar-acidity zone assessment cannot be null");

    return new FeedstockMeasurementSugarAcidityZoneResponse(
        assessment.measurementId().value(),
        assessment.ciderMakerId().value(),
        assessment.batchId().value(),
        assessment.measurementName().value(),
        SugarAcidityZoneRestMapper.toResponse(assessment.assessment())
    );
  }

  public static @NonNull FeedstockMeasurementSugarAcidityZonePointQuery toQuery(
      @NonNull FeedstockMeasurementSugarAcidityZonePointQueryRequest request
  ) {
    requireNonNull(request, "feedstock measurement sugar-acidity zone point query request cannot be null");

    return new FeedstockMeasurementSugarAcidityZonePointQuery(
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

  public static @NonNull FeedstockMeasurementSugarAcidityZoneResponse toResponse(
      @NonNull FeedstockMeasurementSugarAcidityZoneResult result
  ) {
    requireNonNull(result, "feedstock measurement sugar-acidity zone result cannot be null");

    return new FeedstockMeasurementSugarAcidityZoneResponse(
        result.measurementId().value(),
        result.ciderMakerId().value(),
        result.batchId().value(),
        result.measurementName().value(),
        result.plottable(),
        toAssessmentResponse(result.assessment()),
        ChemicalProfileCapabilityRestMapper.toResponse(result.capabilityAssessment())
    );
  }

  public static @NonNull FeedstockMeasurementSugarAcidityZonePointBatchResponse toResponse(
      @NonNull CursorPage<@NonNull FeedstockMeasurementSugarAcidityZoneResult> page
  ) {
    requireNonNull(page, "feedstock measurement sugar-acidity zone point page cannot be null");

    return new FeedstockMeasurementSugarAcidityZonePointBatchResponse(
        page.items().stream()
            .filter(FeedstockMeasurementSugarAcidityZoneResult::plottable)
            .map(FeedstockMeasurementSugarAcidityZoneRestMapper::toResponse)
            .toList(),
        page.items().stream()
            .filter(result -> !result.plottable())
            .map(FeedstockMeasurementSugarAcidityZoneRestMapper::toUnplottableResponse)
            .toList(),
        page.nextCursor()
    );
  }

  private static @NonNull UnplottableFeedstockMeasurementSugarAcidityZonePointResponse toUnplottableResponse(
      @NonNull FeedstockMeasurementSugarAcidityZoneResult result
  ) {
    requireNonNull(result, "feedstock measurement sugar-acidity zone result cannot be null");

    return new UnplottableFeedstockMeasurementSugarAcidityZonePointResponse(
        result.measurementId().value(),
        result.ciderMakerId().value(),
        result.batchId().value(),
        result.measurementName().value(),
        ChemicalProfileCapabilityRestMapper.toResponse(result.capabilityAssessment())
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
