package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.mapper;

import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietySugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietySugarAcidityZonePointBatch;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietySugarAcidityZoneResult;
import org.craft.mcs.cap.core.appleprofile.application.port.in.UnplottableAppleVarietySugarAcidityZonePoint;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietySugarAcidityZonePointBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietySugarAcidityZoneResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.UnplottableAppleVarietySugarAcidityZonePointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.ChemicalProfileCapabilityRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper.SugarAcidityZoneRestMapper;
import org.jspecify.annotations.NonNull;

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
      @NonNull AppleVarietySugarAcidityZonePointBatch batch
  ) {
    requireNonNull(batch, "apple variety sugar-acidity zone point batch cannot be null");

    return new AppleVarietySugarAcidityZonePointBatchResponse(
        batch.points().stream()
            .map(AppleVarietySugarAcidityZoneRestMapper::toResponse)
            .toList(),
        batch.unplottable().stream()
            .map(AppleVarietySugarAcidityZoneRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull UnplottableAppleVarietySugarAcidityZonePointResponse toResponse(
      @NonNull UnplottableAppleVarietySugarAcidityZonePoint unplottable
  ) {
    requireNonNull(unplottable, "unplottable apple variety sugar-acidity zone point cannot be null");

    return new UnplottableAppleVarietySugarAcidityZonePointResponse(
        unplottable.varietyId().value(),
        unplottable.varietyName().value(),
        ChemicalProfileCapabilityRestMapper.toResponse(unplottable.capabilityAssessment())
    );
  }
}
