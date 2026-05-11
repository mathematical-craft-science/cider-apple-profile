package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper;

import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCurrentSugarAcidityZoneResult;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCurrentPotentialAbvPrediction;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCurrentSugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileCapabilityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCurrentPotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCurrentSugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCurrentSugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockBatchCurrentSugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.FeedstockSeasonRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.UnplottableFeedstockBatchCurrentSugarAcidityZonePointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.fermentation.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.shared.platform.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.CursorPage;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.craft.mcs.cap.shared.platform.query.SugarAcidityZoneCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class FeedstockBatchCurrentAssessmentRestMapper {

  private FeedstockBatchCurrentAssessmentRestMapper() {
  }

  public static @NonNull FeedstockBatchCurrentSugarAcidityZonePointQuery toQuery(
      @NonNull FeedstockBatchCurrentSugarAcidityZonePointQueryRequest request
  ) {
    requireNonNull(request, "feedstock batch current sugar-acidity zone point query request cannot be null");

    return new FeedstockBatchCurrentSugarAcidityZonePointQuery(
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

  public static @NonNull FeedstockBatchCurrentSugarAcidityZoneResponse toResponse(
      @NonNull FeedstockBatchCurrentSugarAcidityZoneResult result
  ) {
    requireNonNull(result, "feedstock batch current sugar-acidity zone result cannot be null");

    return new FeedstockBatchCurrentSugarAcidityZoneResponse(
        result.batchId().value(),
        result.ciderMakerId().value(),
        result.currentMeasurementId().value(),
        result.currentMeasurementName().value(),
        result.plottable(),
        result.assessment() == null ? null : SugarAcidityZoneRestMapper.toResponse(result.assessment()), // NOSONAR
        ChemicalProfileCapabilityRestMapper.toResponse(result.capabilityAssessment())
    );
  }

  public static @NonNull FeedstockBatchCurrentSugarAcidityZonePointBatchResponse toResponse(
      @NonNull CursorPage<@NonNull FeedstockBatchCurrentSugarAcidityZoneResult> page
  ) {
    requireNonNull(page, "feedstock batch current sugar-acidity zone point page cannot be null");

    return new FeedstockBatchCurrentSugarAcidityZonePointBatchResponse(
        page.items().stream()
            .filter(FeedstockBatchCurrentSugarAcidityZoneResult::plottable)
            .map(FeedstockBatchCurrentAssessmentRestMapper::toResponse)
            .toList(),
        page.items().stream()
            .filter(result -> !result.plottable())
            .map(FeedstockBatchCurrentAssessmentRestMapper::toUnplottableResponse)
            .toList(),
        page.nextCursor()
    );
  }

  public static @NonNull FeedstockBatchCurrentPotentialAbvResponse toResponse(
      @NonNull FeedstockBatchCurrentPotentialAbvPrediction prediction
  ) {
    requireNonNull(prediction, "feedstock batch current potential ABV prediction cannot be null");

    return new FeedstockBatchCurrentPotentialAbvResponse(
        prediction.batchId().value(),
        prediction.ciderMakerId().value(),
        prediction.currentMeasurementId().value(),
        prediction.currentMeasurementName().value(),
        prediction.originalGravityValueKind(),
        PotentialAbvPredictionRestMapper.toResponse(prediction.prediction())
    );
  }

  private static @NonNull UnplottableFeedstockBatchCurrentSugarAcidityZonePointResponse toUnplottableResponse(
      @NonNull FeedstockBatchCurrentSugarAcidityZoneResult result
  ) {
    requireNonNull(result, "feedstock batch current sugar-acidity zone result cannot be null");

    return new UnplottableFeedstockBatchCurrentSugarAcidityZonePointResponse(
        result.batchId().value(),
        result.ciderMakerId().value(),
        result.currentMeasurementId().value(),
        result.currentMeasurementName().value(),
        ChemicalProfileCapabilityRestMapper.toResponse(result.capabilityAssessment())
    );
  }

  private static @Nullable FeedstockSeason toSeason(@Nullable FeedstockSeasonRequest request) {
    return request == null ? null : FeedstockSeason.of(request.startsOn(), request.endsOn());
  }
}
