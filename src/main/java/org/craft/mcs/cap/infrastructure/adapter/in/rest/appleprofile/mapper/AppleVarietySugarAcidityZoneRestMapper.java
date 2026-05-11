package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.mapper;

import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyPointFilters;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietySugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietySugarAcidityZonePointQuery;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietySugarAcidityZonePointQueryRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietySugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietySugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.UnplottableAppleVarietySugarAcidityZonePointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileCapabilityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.shared.platform.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.CursorPage;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.craft.mcs.cap.shared.platform.query.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.shared.platform.query.TextSearchCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class AppleVarietySugarAcidityZoneRestMapper {

  private AppleVarietySugarAcidityZoneRestMapper() {
  }

  public static @NonNull AppleVarietySugarAcidityZoneResponse toResponse(
      @NonNull AppleVarietySugarAcidityZoneAssessment assessment
  ) {
    requireNonNull(assessment, "apple variety sugar-acidity zone assessment cannot be null");

    return new AppleVarietySugarAcidityZoneResponse(
        assessment.varietyId().value(),
        assessment.varietyName().value(),
        SugarAcidityZoneRestMapper.toResponse(assessment.assessment())
    );
  }

  public static @NonNull AppleVarietySugarAcidityZonePointQuery toQuery(
      @NonNull AppleVarietySugarAcidityZonePointQueryRequest request
  ) {
    requireNonNull(request, "apple variety sugar-acidity zone point query request cannot be null");

    return new AppleVarietySugarAcidityZonePointQuery(
        new AppleVarietyPointFilters(
            toTextCriteria(request.nameContains()),
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

  public static @NonNull AppleVarietySugarAcidityZoneResponse toResponse(
      @NonNull AppleVarietySugarAcidityZoneResult result
  ) {
    requireNonNull(result, "apple variety sugar-acidity zone result cannot be null");

    return new AppleVarietySugarAcidityZoneResponse(
        result.varietyId().value(),
        result.varietyName().value(),
        result.plottable(),
        result.assessment() == null ? null : SugarAcidityZoneRestMapper.toResponse(result.assessment()), // NOSONAR
        ChemicalProfileCapabilityRestMapper.toResponse(result.capabilityAssessment())
    );
  }

  public static @NonNull AppleVarietySugarAcidityZonePointBatchResponse toResponse(
      @NonNull CursorPage<@NonNull AppleVarietySugarAcidityZoneResult> page
  ) {
    requireNonNull(page, "apple variety sugar-acidity zone point page cannot be null");

    return new AppleVarietySugarAcidityZonePointBatchResponse(
        page.items().stream()
            .filter(AppleVarietySugarAcidityZoneResult::plottable)
            .map(AppleVarietySugarAcidityZoneRestMapper::toResponse)
            .toList(),
        page.items().stream()
            .filter(result -> !result.plottable())
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
        ChemicalProfileCapabilityRestMapper.toResponse(result.capabilityAssessment())
    );
  }

  private static @Nullable TextSearchCriteria toTextCriteria(@Nullable String nameContains) {
    return nameContains == null || nameContains.isBlank() ? null : new TextSearchCriteria(nameContains);
  }
}
