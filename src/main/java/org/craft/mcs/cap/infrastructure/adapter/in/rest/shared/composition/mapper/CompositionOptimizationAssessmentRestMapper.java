package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationCriterionInput;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationObjectiveView;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationObjectiveAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationPenaltyResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationYanReadinessRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class CompositionOptimizationAssessmentRestMapper {

  private CompositionOptimizationAssessmentRestMapper() {
  }

  public static @NonNull ProfileAxisNormalizerCode defaultOptimizationNormalizer() {
    return CompositionPlanningRestDefaults.profileTargetNormalizer();
  }

  public static @NonNull ProfileTargetObjectiveFunctionCode defaultOptimizationObjectiveFunction() {
    return CompositionPlanningRestDefaults.profileTargetObjectiveFunction();
  }

  public static @NonNull List<@NonNull OptimizationCriterionInput> toCriteria(
      @Nullable CompositionOptimizationYanReadinessRequest yanReadiness
  ) {
    if (yanReadiness == null) {
      return List.of();
    }
    final var request = requireNonNull(yanReadiness);
    final var requestedWeight = request.weight();
    final var weight = requestedWeight == null
        ? CompositionOptimizationYanReadinessRequest.DEFAULT_WEIGHT
        : requestedWeight;
    final var requestedMissingPolicy = request.missingPolicy();
    final var missingPolicy = requestedMissingPolicy == null
        ? CompositionOptimizationYanReadinessRequest.DEFAULT_MISSING_POLICY
        : requestedMissingPolicy;
    final var requestedMissingPenalty = request.missingPenalty();
    final var missingPenalty = requestedMissingPenalty == null
        ? CompositionOptimizationYanReadinessRequest.DEFAULT_MISSING_PENALTY
        : requestedMissingPenalty;

    return List.of(new OptimizationCriterionInput.YanReadiness( // NOSONAR
        yanValue(request.minimum()),
        yanValue(request.maximum()),
        weight,
        OptimizationCriterionInput.MissingPolicy.valueOf(missingPolicy.name()),
        missingPenalty
    ));
  }

  private static @Nullable YeastAssimilableNitrogenValue yanValue(@Nullable BigDecimal value) {
    return value == null ? null : YeastAssimilableNitrogenValue.of(value);
  }

  public static @NonNull CompositionOptimizationObjectiveAssessmentResponse toResponse(
      @NonNull OptimizationObjectiveView assessment
  ) {
    requireNonNull(assessment, "composition optimization objective assessment cannot be null");
    return new CompositionOptimizationObjectiveAssessmentResponse(
        assessment.score(),
        assessment.penalties()
            .stream()
            .map(CompositionOptimizationAssessmentRestMapper::toResponse)
            .toList()
    );
  }

  public static @NonNull CompositionOptimizationObjectiveAssessmentResponse toResponse(
      FeedstockCompositionOptimizationView.@NonNull Objective assessment
  ) {
    return new CompositionOptimizationObjectiveAssessmentResponse(
        assessment.score(),
        assessment.penalties().stream()
            .map(CompositionOptimizationAssessmentRestMapper::toResponse)
            .toList()
    );
  }

  private static @NonNull CompositionOptimizationPenaltyResponse toResponse(
      OptimizationObjectiveView.@NonNull Penalty penalty
  ) {
    requireNonNull(penalty, "composition optimization penalty cannot be null");
    return new CompositionOptimizationPenaltyResponse(
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationPenaltyCode
            .valueOf(penalty.code().name()),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationPenaltySeverity
            .valueOf(penalty.severity().name()),
        penalty.value(),
        penalty.characteristic(),
        quantity(penalty.actualValue(), penalty.characteristic()),
        quantity(penalty.threshold(), penalty.characteristic())
    );
  }

  private static @NonNull CompositionOptimizationPenaltyResponse toResponse(
      FeedstockCompositionOptimizationView.@NonNull Penalty penalty
  ) {
    return new CompositionOptimizationPenaltyResponse(
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationPenaltyCode
            .valueOf(penalty.code().name()),
        org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionOptimizationPenaltySeverity
            .valueOf(penalty.severity().name()),
        penalty.value(),
        penalty.characteristic(),
        quantity(penalty.actualValue(), penalty.characteristic()),
        quantity(penalty.threshold(), penalty.characteristic())
    );
  }

  private static @Nullable DecimalQuantityResponse quantity(
      @Nullable BigDecimal value,
      @Nullable MeasurementCharacteristic characteristic
  ) {
    return value == null || characteristic == null
           ? null
           : new DecimalQuantityResponse(value, CanonicalUnitLabels.forCharacteristic(characteristic));
  }
}
