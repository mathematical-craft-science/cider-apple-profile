package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityZoneAssessmentResponse;
import org.craft.mcs.cap.shared.domain.classification.profile.sugaracidity.SugarAcidityLevel;
import org.craft.mcs.cap.shared.domain.classification.profile.sugaracidity.SugarAcidityZoneAssessment;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class SugarAcidityZoneRestMapper {

  private SugarAcidityZoneRestMapper() {
  }

  public static @NonNull SugarAcidityZoneAssessmentResponse toResponse(
      @NonNull SugarAcidityZoneAssessment assessment
  ) {
    requireNonNull(assessment, "sugar-acidity zone assessment cannot be null");

    return new SugarAcidityZoneAssessmentResponse(
        assessment.classified(),
        assessment.zone() == null ? null : assessment.zone().name(), // NOSONAR
        assessment.sugar().name(),
        assessment.acidity().name(),
        levelName(assessment.tannin())
    );
  }

  private static @Nullable String levelName(@Nullable SugarAcidityLevel level) {
    return level == null ? null : level.name();
  }
}
