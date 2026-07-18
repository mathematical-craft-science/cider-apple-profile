package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.mapper;

import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietyPointFilters;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.AppleVarietySugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.AppleVarietySugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.AppleVarietySugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto.UnplottableAppleVarietySugarAcidityZonePointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileCapabilityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityZoneAssessmentResponse;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.CursorPage;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.application.model.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.shared.platform.query.TextSearchCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;


public final class AppleVarietySugarAcidityZoneRestMapper {

  private AppleVarietySugarAcidityZoneRestMapper() {
  }

  public static @NonNull AppleVarietySugarAcidityZonePointQuery toQuery(
      @NonNull AppleVarietySugarAcidityZonePointQueryRequest request
  ) {
    requireNonNull(request, "apple variety sugar-acidity zone point query request cannot be null");

    return new AppleVarietySugarAcidityZonePointQuery(
        new AppleVarietyPointFilters(
            toTextCriteria(request.nameContains()),
            toZoneCriteria(request.zone()),
            toCapabilityCriteria(request.capability(), request.capable())
        ),
        new CursorPageRequest(
            request.after(),
            requireNonNullElse(request.limit(), CursorPageRequest.DEFAULT_LIMIT)
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
        toAssessmentResponse(evaluation.assessment()),
        toPotentialAbvResponse(result),
        ChemicalProfileCapabilityRestMapper.toResponse(evaluation.capabilityAssessment())
    );
  }

  public static @NonNull AppleVarietySugarAcidityZonePointBatchResponse toResponse(
      @NonNull CursorPage<@NonNull AppleVarietySugarAcidityZoneResult> page
  ) {
    requireNonNull(page, "apple variety sugar-acidity zone point page cannot be null");

    return new AppleVarietySugarAcidityZonePointBatchResponse(
        page.items().stream()
            .filter(result -> result.evaluation().plottable())
            .map(AppleVarietySugarAcidityZoneRestMapper::toResponse)
            .toList(),
        page.items().stream()
            .filter(result -> !result.evaluation().plottable())
            .map(AppleVarietySugarAcidityZoneRestMapper::toUnplottableResponse)
            .toList(),
        page.nextCursor()
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

  private static @Nullable PotentialAbvPredictionResponse toPotentialAbvResponse(
      @NonNull AppleVarietySugarAcidityZoneResult result
  ) {
    final var prediction = result.potentialAbv();
    return prediction == null ? null : PotentialAbvPredictionRestMapper.toResponse(prediction);
  }
}
