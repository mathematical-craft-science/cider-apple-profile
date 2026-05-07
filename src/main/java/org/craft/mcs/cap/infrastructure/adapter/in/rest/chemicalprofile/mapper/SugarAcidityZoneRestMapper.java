package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.mapper;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ExactSugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.RangeSugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.StatisticalSugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityProfilePointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.SugarAcidityZoneAssessmentResponse;
import org.craft.mcs.cap.shared.domain.sugaracidity.point.SugarAcidityAxisPoint;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityLevel;
import org.craft.mcs.cap.shared.domain.sugaracidity.point.SugarAcidityProfilePoint;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityZoneAssessment;
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
        levelName(assessment.tannin()),
        assessment.balancedForSingleVariety(),
        pointResponse(assessment.point())
    );
  }

  private static @NonNull SugarAcidityProfilePointResponse pointResponse(
      @NonNull SugarAcidityProfilePoint point
  ) {
    requireNonNull(point, "sugar-acidity profile point cannot be null");

    return new SugarAcidityProfilePointResponse(
        pointResponse(point.sugar()),
        pointResponse(point.acidity()),
        point.tannin() == null ? null : pointResponse(point.tannin()) // NOSONAR
    );
  }

  private static @NonNull SugarAcidityAxisPointResponse pointResponse(
      @NonNull SugarAcidityAxisPoint point
  ) {
    requireNonNull(point, "sugar-acidity axis point cannot be null");

    return switch (point.valueKind()) {
      case EXACT -> new ExactSugarAcidityAxisPointResponse(
          point.value()
      );
      case RANGE_MIDPOINT -> new RangeSugarAcidityAxisPointResponse(
          point.value(),
          requireNonNull(point.min(), "range axis point minimum cannot be null"),
          requireNonNull(point.max(), "range axis point maximum cannot be null")
      );
      case STATISTICAL_MEAN, STATISTICAL_MEDIAN -> statisticalPointResponse(point);
    };
  }

  private static @NonNull StatisticalSugarAcidityAxisPointResponse statisticalPointResponse(
      @NonNull SugarAcidityAxisPoint point
  ) {
    requireNonNull(point, "statistical sugar-acidity axis point cannot be null");

    return new StatisticalSugarAcidityAxisPointResponse(
        point.value(),
        point.valueKind().name(),
        point.standardDeviation(),
        point.sampleSize()
    );
  }

  private static @Nullable String levelName(@Nullable SugarAcidityLevel level) {
    return level == null ? null : level.name();
  }
}
