package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.mapper;

import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.ExactSugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.CalculatedSugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.RangeSugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.StatisticalSugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityZoneAssessmentResponse;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.point.domain.SugarAcidityAxisPoint;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.point.domain.SugarAcidityProfilePoint;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
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
        toResponse(point.sugar(), CanonicalUnitLabels.SUGAR),
        toResponse(point.acidity(), CanonicalUnitLabels.TITRATABLE_ACIDITY),
        toTanninResponse(point.tannin())
    );
  }

  private static @Nullable SugarAcidityAxisPointResponse toTanninResponse(
      @Nullable SugarAcidityAxisPoint point
  ) {
    return point == null ? null : toResponse(point, CanonicalUnitLabels.TANNINITY);
  }

  public static @NonNull SugarAcidityAxisPointResponse toResponse(
      @NonNull SugarAcidityAxisPoint point,
      @NonNull String unit
  ) {
    requireNonNull(point, "sugar-acidity axis point cannot be null");
    requireNonNull(unit, "sugar-acidity axis point unit cannot be null");

    return switch (point.valueKind()) {
      case EXACT -> new ExactSugarAcidityAxisPointResponse(
          point.value(),
          unit
      );
      case RANGE_MIDPOINT -> new RangeSugarAcidityAxisPointResponse(
          point.value(),
          requireNonNull(point.min(), "range axis point minimum cannot be null"),
          requireNonNull(point.max(), "range axis point maximum cannot be null"),
          unit
      );
      case STATISTICAL_MEAN, STATISTICAL_MEDIAN -> statisticalPointResponse(point, unit);
      case CALCULATED -> new CalculatedSugarAcidityAxisPointResponse(point.value(), unit);
    };
  }

  private static @NonNull StatisticalSugarAcidityAxisPointResponse statisticalPointResponse(
      @NonNull SugarAcidityAxisPoint point,
      @NonNull String unit
  ) {
    requireNonNull(point, "statistical sugar-acidity axis point cannot be null");
    requireNonNull(unit, "statistical sugar-acidity axis point unit cannot be null");

    return new StatisticalSugarAcidityAxisPointResponse(
        point.value(),
        point.valueKind(),
        point.standardDeviation(),
        point.sampleSize(),
        unit
    );
  }
}
