package org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.mapper;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.ExactSugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.CalculatedSugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.RangeSugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.StatisticalSugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.SugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.SugarAcidityZoneAssessmentResponse;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.point.domain.SugarAcidityAxisPoint;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.point.domain.SugarAcidityProfilePoint;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.jspecify.annotations.NonNull;

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
        assessment.zone(),
        assessment.sugar(),
        assessment.acidity(),
        assessment.tannin(),
        assessment.balancedForSingleVariety(),
        toResponse(assessment.point())
    );
  }

  public static @NonNull SugarAcidityProfilePointResponse toResponse(
      @NonNull SugarAcidityProfilePoint point
  ) {
    requireNonNull(point, "sugar-acidity profile point cannot be null");

    return new SugarAcidityProfilePointResponse(
        toResponse(point.sugar()),
        toResponse(point.acidity()),
        point.tannin() == null ? null : toResponse(point.tannin()) // NOSONAR
    );
  }

  public static @NonNull SugarAcidityAxisPointResponse toResponse(
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
      case CALCULATED -> new CalculatedSugarAcidityAxisPointResponse(point.value());
    };
  }

  private static @NonNull StatisticalSugarAcidityAxisPointResponse statisticalPointResponse(
      @NonNull SugarAcidityAxisPoint point
  ) {
    requireNonNull(point, "statistical sugar-acidity axis point cannot be null");

    return new StatisticalSugarAcidityAxisPointResponse(
        point.value(),
        point.valueKind(),
        point.standardDeviation(),
        point.sampleSize()
    );
  }
}
