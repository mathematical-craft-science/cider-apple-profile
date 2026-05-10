package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper;

import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSamplePointFilters;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileCapabilityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSampleSugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.UnplottableFeedstockSampleSugarAcidityZonePointResponse;
import org.craft.mcs.cap.shared.application.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.application.query.CursorPage;
import org.craft.mcs.cap.shared.application.query.CursorPageRequest;
import org.craft.mcs.cap.shared.application.query.SugarAcidityZoneCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class FeedstockSampleSugarAcidityZoneRestMapper {

  private FeedstockSampleSugarAcidityZoneRestMapper() {
  }

  public static @NonNull FeedstockSampleSugarAcidityZoneResponse toResponse(
      @NonNull FeedstockSampleSugarAcidityZoneAssessment assessment
  ) {
    requireNonNull(assessment, "feedstock sample sugar-acidity zone assessment cannot be null");

    return new FeedstockSampleSugarAcidityZoneResponse(
        assessment.sampleId().value(),
        assessment.ciderMakerId().value(),
        assessment.batchId().value(),
        assessment.sampleName().value(),
        SugarAcidityZoneRestMapper.toResponse(assessment.assessment())
    );
  }

  public static @NonNull FeedstockSampleSugarAcidityZonePointQuery toQuery(
      @NonNull FeedstockSampleSugarAcidityZonePointQueryRequest request
  ) {
    requireNonNull(request, "feedstock sample sugar-acidity zone point query request cannot be null");

    return new FeedstockSampleSugarAcidityZonePointQuery(
        CiderMakerId.of(request.ciderMakerId()),
        new FeedstockSamplePointFilters(
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

  public static @NonNull FeedstockSampleSugarAcidityZoneResponse toResponse(
      @NonNull FeedstockSampleSugarAcidityZoneResult result
  ) {
    requireNonNull(result, "feedstock sample sugar-acidity zone result cannot be null");

    return new FeedstockSampleSugarAcidityZoneResponse(
        result.sampleId().value(),
        result.ciderMakerId().value(),
        result.batchId().value(),
        result.sampleName().value(),
        result.plottable(),
        result.assessment() == null ? null : SugarAcidityZoneRestMapper.toResponse(result.assessment()), // NOSONAR
        ChemicalProfileCapabilityRestMapper.toResponse(result.capabilityAssessment())
    );
  }

  public static @NonNull FeedstockSampleSugarAcidityZonePointBatchResponse toResponse(
      @NonNull CursorPage<@NonNull FeedstockSampleSugarAcidityZoneResult> page
  ) {
    requireNonNull(page, "feedstock sample sugar-acidity zone point page cannot be null");

    return new FeedstockSampleSugarAcidityZonePointBatchResponse(
        page.items().stream()
            .filter(FeedstockSampleSugarAcidityZoneResult::plottable)
            .map(FeedstockSampleSugarAcidityZoneRestMapper::toResponse)
            .toList(),
        page.items().stream()
            .filter(result -> !result.plottable())
            .map(FeedstockSampleSugarAcidityZoneRestMapper::toUnplottableResponse)
            .toList(),
        page.nextCursor()
    );
  }

  private static @NonNull UnplottableFeedstockSampleSugarAcidityZonePointResponse toUnplottableResponse(
      @NonNull FeedstockSampleSugarAcidityZoneResult result
  ) {
    requireNonNull(result, "feedstock sample sugar-acidity zone result cannot be null");

    return new UnplottableFeedstockSampleSugarAcidityZonePointResponse(
        result.sampleId().value(),
        result.ciderMakerId().value(),
        result.batchId().value(),
        result.sampleName().value(),
        ChemicalProfileCapabilityRestMapper.toResponse(result.capabilityAssessment())
    );
  }

  private static @Nullable FeedstockSeason toSeason(@Nullable FeedstockSeasonRequest request) {
    return request == null ? null : FeedstockSeason.of(request.startsOn(), request.endsOn());
  }
}
