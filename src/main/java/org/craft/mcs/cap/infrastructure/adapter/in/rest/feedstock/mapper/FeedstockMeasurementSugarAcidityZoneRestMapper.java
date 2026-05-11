package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper;

import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementSugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileCapabilityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockMeasurementSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockMeasurementSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockMeasurementSugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.UnplottableFeedstockMeasurementSugarAcidityZonePointResponse;
import org.craft.mcs.cap.shared.platform.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.CursorPage;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.craft.mcs.cap.shared.platform.query.SugarAcidityZoneCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


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
            request.batchId() == null ? null : FeedstockBatchCriteria.of(FeedstockBatchId.of(request.batchId())), // NOSONAR
            toSeason(request.season()),
            request.zone() == null ? null : SugarAcidityZoneCriteria.of(request.zone()), // NOSONAR
            request.capability() == null
                ? null
                : new ChemicalProfileCapabilityCriteria(request.capability(), request.capable()) // NOSONAR
        ),
        new CursorPageRequest(
            request.after(),
            request.limit() == null ? CursorPageRequest.DEFAULT_LIMIT : request.limit()
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
        result.assessment() == null ? null : SugarAcidityZoneRestMapper.toResponse(result.assessment()), // NOSONAR
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
}
