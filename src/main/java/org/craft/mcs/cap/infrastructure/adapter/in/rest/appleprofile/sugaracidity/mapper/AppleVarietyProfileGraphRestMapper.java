package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.mapper;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model.AppleVarietyAnalysisPagePosition;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model.AppleVarietyAnalysisFilters;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.model.AppleVarietyGraphAnalysisView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.port.in.AppleVarietySugarAcidityZonePointQuery;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.AppleVarietyNorthAmericanProfileResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.AppleVarietySugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.AppleVarietySugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.AppleVarietySugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.UnplottableAppleVarietySugarAcidityZonePointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.cursor.AppleVarietySugarAcidityCursorAdapter;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.JolicoeurSugarAcidityGraphRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.ProfileCharacteristicValueResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.mapper.ProfileCharacteristicRestMapper;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPage;
import org.craft.mcs.cap.shared.application.query.pagination.CursorPageRequest;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.JolicoeurSugarAcidityGraphRestMapper
    .missingCharacteristics;
import static org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.mapper.JolicoeurSugarAcidityGraphCriteriaRestMapper
    .toGraphCriteria;


@Component
@RequiredArgsConstructor
public final class AppleVarietyProfileGraphRestMapper {

  private final @NonNull AppleVarietySugarAcidityCursorAdapter cursorAdapter;

  public @NonNull AppleVarietySugarAcidityZonePointQuery toQuery(
      @NonNull AppleVarietySugarAcidityZonePointQueryRequest request
  ) {
    requireNonNull(request, "apple variety sugar-acidity zone point query request cannot be null");
    final var filters = new AppleVarietyAnalysisFilters(toTextCriteria(request.nameContains()));
    final var graphCriteria = toGraphCriteria(request.zone(), request.plottable());

    return new AppleVarietySugarAcidityZonePointQuery(
        filters,
        graphCriteria,
        new CursorPageRequest<>(
            cursorAdapter.decode(request.after(), filters, graphCriteria),
            requireNonNullElse(request.limit(), QueryLimits.DEFAULT)
        )
    );
  }

  public static @NonNull AppleVarietySugarAcidityZoneResponse toResponse(
      @NonNull AppleVarietyGraphAnalysisView result
  ) {
    requireNonNull(result, "apple variety graph analysis view cannot be null");
    final var graphEvaluation = result.graphEvaluation();

    return new AppleVarietySugarAcidityZoneResponse(
        result.varietyId().value(),
        result.varietyName().value(),
        graphEvaluation.plottable(),
        toAssessmentResponse(result),
        toTanninResponse(result),
        toNorthAmericanProfileResponse(result),
        toPotentialAbvResponse(result),
        missingCharacteristics(graphEvaluation)
    );
  }

  public @NonNull AppleVarietySugarAcidityZonePointBatchResponse toResponse(
      @NonNull CursorPage<
          @NonNull AppleVarietyGraphAnalysisView,
          @NonNull AppleVarietyAnalysisPagePosition
      > page,
      @NonNull AppleVarietySugarAcidityZonePointQuery query
  ) {
    requireNonNull(page, "apple variety sugar-acidity zone point page cannot be null");
    requireNonNull(query, "apple variety sugar-acidity zone point query cannot be null");

    return new AppleVarietySugarAcidityZonePointBatchResponse(
        page.items().stream()
            .filter(result -> result.graphEvaluation().plottable())
            .map(AppleVarietyProfileGraphRestMapper::toResponse)
            .toList(),
        page.items().stream()
            .filter(result -> !result.graphEvaluation().plottable())
            .map(AppleVarietyProfileGraphRestMapper::toUnplottableResponse)
            .toList(),
        cursorAdapter.encode(page.nextPosition(), query.filters(), query.graphCriteria())
    );
  }

  private static @NonNull UnplottableAppleVarietySugarAcidityZonePointResponse toUnplottableResponse(
      @NonNull AppleVarietyGraphAnalysisView result
  ) {
    requireNonNull(result, "apple variety graph analysis view cannot be null");

    return new UnplottableAppleVarietySugarAcidityZonePointResponse(
        result.varietyId().value(),
        result.varietyName().value(),
        missingCharacteristics(result.graphEvaluation())
    );
  }

  private static @Nullable TextSearchCriteria toTextCriteria(@Nullable String nameContains) {
    return nameContains == null || nameContains.isBlank() ? null : new TextSearchCriteria(nameContains);
  }

  private static @Nullable SugarAcidityGraphAssessmentResponse toAssessmentResponse(
      @NonNull AppleVarietyGraphAnalysisView result
  ) {
    if (!result.graphEvaluation().plottable()) {
      return null;
    }
    return JolicoeurSugarAcidityGraphRestMapper.toResponse(
        result.graphEvaluation(),
        requireNonNull(result.specificGravity()),
        requireNonNull(result.titratableAcidity())
    );
  }

  private static @Nullable ProfileCharacteristicValueResponse toTanninResponse(
      @NonNull AppleVarietyGraphAnalysisView result
  ) {
    final var tannin = result.tannin();
    return tannin == null
        ? null
        : ProfileCharacteristicRestMapper.toResponse(
            tannin,
            TanninValue::asBigDecimal,
            CanonicalUnitLabels.TANNIN
        );
  }

  private static @Nullable AppleVarietyNorthAmericanProfileResponse toNorthAmericanProfileResponse(
      @NonNull AppleVarietyGraphAnalysisView result
  ) {
    final var assessment = result.northAmericanAssessment();
    return assessment == null
        ? null
        : new AppleVarietyNorthAmericanProfileResponse(
            assessment.sugar(),
            assessment.acidity(),
            assessment.tannin()
        );
  }

  private static @Nullable PotentialAbvPredictionResponse toPotentialAbvResponse(
      @NonNull AppleVarietyGraphAnalysisView result
  ) {
    final var prediction = result.potentialAbv();
    return prediction == null ? null : PotentialAbvPredictionRestMapper.toResponse(prediction);
  }
}
