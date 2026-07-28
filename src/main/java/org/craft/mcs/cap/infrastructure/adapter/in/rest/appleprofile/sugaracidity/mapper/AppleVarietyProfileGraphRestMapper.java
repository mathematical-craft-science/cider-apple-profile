package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.mapper;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.catalog.model.AppleVarietyCatalogPagePosition;
import org.craft.mcs.cap.core.appleprofile.application.analysis.common.model.AppleVarietyAnalysisFilters;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.AppleVarietySugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.AppleVarietySugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.AppleVarietySugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.UnplottableAppleVarietySugarAcidityZonePointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.cursor.AppleVarietySugarAcidityCursorAdapter;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileCapabilityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.ProfileGraphRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphAssessmentResponse;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessment;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPage;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.ProfileGraphCriteriaRestMapper
    .toCapabilityCriteria;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.ProfileGraphCriteriaRestMapper
    .toZoneCriteria;


@Component
@RequiredArgsConstructor
public final class AppleVarietyProfileGraphRestMapper {

  private final @NonNull AppleVarietySugarAcidityCursorAdapter cursorAdapter;

  public @NonNull AppleVarietySugarAcidityZonePointQuery toQuery(
      @NonNull AppleVarietySugarAcidityZonePointQueryRequest request
  ) {
    requireNonNull(request, "apple variety sugar-acidity zone point query request cannot be null");
    final var filters = new AppleVarietyAnalysisFilters(
        toTextCriteria(request.nameContains()),
        toZoneCriteria(request.zone()),
        toCapabilityCriteria(request.capability(), request.capable())
    );

    return new AppleVarietySugarAcidityZonePointQuery(
        filters,
        new CursorPageRequest<>(
            cursorAdapter.decode(request.after(), filters),
            requireNonNullElse(request.limit(), QueryLimits.DEFAULT)
        )
    );
  }

  public static @NonNull AppleVarietySugarAcidityZoneResponse toResponse(
      @NonNull AppleVarietySugarAcidityZoneResult result
  ) {
    requireNonNull(result, "apple variety sugar-acidity zone result cannot be null");
    final var evaluation = result.evaluation();

    return new AppleVarietySugarAcidityZoneResponse(
        result.varietyId().value(),
        result.varietyName().value(),
        evaluation.plottable(),
        toAssessmentResponse(evaluation),
        toPotentialAbvResponse(result),
        ChemicalProfileCapabilityRestMapper.toResponse(evaluation.capabilityAssessment())
    );
  }

  public @NonNull AppleVarietySugarAcidityZonePointBatchResponse toResponse(
      @NonNull CursorPage<
          @NonNull AppleVarietySugarAcidityZoneResult,
          @NonNull AppleVarietyCatalogPagePosition
      > page,
      @NonNull AppleVarietySugarAcidityZonePointQuery query
  ) {
    requireNonNull(page, "apple variety sugar-acidity zone point page cannot be null");
    requireNonNull(query, "apple variety sugar-acidity zone point query cannot be null");

    return new AppleVarietySugarAcidityZonePointBatchResponse(
        page.items().stream()
            .filter(result -> result.evaluation().plottable())
            .map(AppleVarietyProfileGraphRestMapper::toResponse)
            .toList(),
        page.items().stream()
            .filter(result -> !result.evaluation().plottable())
            .map(AppleVarietyProfileGraphRestMapper::toUnplottableResponse)
            .toList(),
        cursorAdapter.encode(page.nextPosition(), query.filters())
    );
  }

  private static @NonNull UnplottableAppleVarietySugarAcidityZonePointResponse toUnplottableResponse(
      @NonNull AppleVarietySugarAcidityZoneResult result
  ) {
    requireNonNull(result, "apple variety sugar-acidity zone result cannot be null");

    return new UnplottableAppleVarietySugarAcidityZonePointResponse(
        result.varietyId().value(),
        result.varietyName().value(),
        ChemicalProfileCapabilityRestMapper.toResponse(result.evaluation().capabilityAssessment())
    );
  }

  private static @Nullable TextSearchCriteria toTextCriteria(@Nullable String nameContains) {
    return nameContains == null || nameContains.isBlank() ? null : new TextSearchCriteria(nameContains);
  }

  private static @Nullable SugarAcidityGraphAssessmentResponse toAssessmentResponse(
      @NonNull ChemicalProfileAssessment evaluation
  ) {
    return evaluation.assessment() == null ? null : ProfileGraphRestMapper.toResponse(evaluation);
  }

  private static @Nullable PotentialAbvPredictionResponse toPotentialAbvResponse(
      @NonNull AppleVarietySugarAcidityZoneResult result
  ) {
    final var prediction = result.potentialAbv();
    return prediction == null ? null : PotentialAbvPredictionRestMapper.toResponse(prediction);
  }
}
