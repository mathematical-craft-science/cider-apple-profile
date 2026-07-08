package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationCriterion;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationObjectiveAssessment;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationPenalty;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.YeastAssimilableNitrogenReadinessCriterion;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationObjectiveAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationPenaltyResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationYanReadinessRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;


public final class CompositionOptimizationRestMapper {

  private CompositionOptimizationRestMapper() {
  }

  public static @NonNull ProfileAxisNormalizerCode defaultOptimizationNormalizer() {
    return CompositionPlanningRestDefaults.profileTargetNormalizer();
  }

  public static @NonNull ProfileTargetObjectiveFunctionCode defaultOptimizationObjectiveFunction() {
    return CompositionPlanningRestDefaults.profileTargetObjectiveFunction();
  }

  public static @NonNull List<@NonNull CompositionOptimizationCriterion> toCriteria(
      @Nullable CompositionOptimizationYanReadinessRequest yanReadiness
  ) {
    if (yanReadiness == null) {
      return List.of();
    }

    return List.of(new YeastAssimilableNitrogenReadinessCriterion( // NOSONAR
        yanValue(yanReadiness.minimum()),
        yanValue(yanReadiness.maximum()),
        requireNonNullElse(yanReadiness.weight(), CompositionOptimizationYanReadinessRequest.DEFAULT_WEIGHT),
        requireNonNullElse(yanReadiness.missingPolicy(),
            CompositionOptimizationYanReadinessRequest.DEFAULT_MISSING_POLICY),
        requireNonNullElse(yanReadiness.missingPenalty(),
            CompositionOptimizationYanReadinessRequest.DEFAULT_MISSING_PENALTY)
    ));
  }

  private static @Nullable YeastAssimilableNitrogenValue yanValue(@Nullable BigDecimal value) {
    return value == null ? null : YeastAssimilableNitrogenValue.of(value);
  }

  public static @NonNull CompositionOptimizationObjectiveAssessmentResponse toResponse(
      @NonNull CompositionOptimizationObjectiveAssessment assessment
  ) {
    requireNonNull(assessment, "composition optimization objective assessment cannot be null");
    return new CompositionOptimizationObjectiveAssessmentResponse(
        assessment.score(),
        assessment.penalties()
            .stream()
            .map(CompositionOptimizationRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull CompositionOptimizationPenaltyResponse toResponse(
      @NonNull CompositionOptimizationPenalty penalty
  ) {
    requireNonNull(penalty, "composition optimization penalty cannot be null");
    return new CompositionOptimizationPenaltyResponse(
        penalty.code(),
        penalty.severity(),
        penalty.value(),
        penalty.axis(),
        quantity(penalty.actualValue(), penalty.axis()),
        quantity(penalty.threshold(), penalty.axis())
    );
  }

  private static @Nullable DecimalQuantityResponse quantity(
      @Nullable BigDecimal value,
      @Nullable ChemicalAxis axis
  ) {
    return value == null || axis == null
           ? null
           : new DecimalQuantityResponse(value, CanonicalUnitLabels.forAxis(axis));
  }
}
