package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.mapper;

import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietySugarAcidityZoneAssessment;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto.AppleVarietySugarAcidityZoneResponse;
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
        assessment.varietyId().value().toString(),
        assessment.varietyName().value(),
        SugarAcidityZoneRestMapper.toResponse(assessment.assessment())
    );
  }
}
