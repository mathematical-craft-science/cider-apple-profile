package org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityCoordinates;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTargetGeometryShape;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTargetKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Result of scoring a profile point against a target objective.
 *
 * <p>The object is intentionally verbose because the API needs to explain optimization decisions:
 * the original point, nearest target point, optional tannin values, selected objective function,
 * metric, weights and missing measurements all travel together. This lets a UI show why one
 * candidate ranked better than another instead of exposing only a scalar score.
 */
public record ProfileTargetDistance(
    @NonNull ProfileTargetDistanceStatus status,
    @NonNull SugarAcidityCoordinates point,
    @NonNull SugarAcidityCoordinates nearestTargetPoint,
    @Nullable BigDecimal tannin,
    @Nullable BigDecimal nearestTargetTannin,
    @Nullable BigDecimal value,
    boolean insideSugarAcidityTarget,
    boolean insideTarget,
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunction,
    @NonNull ProfileDistanceMetric metric,
    @NonNull ProfileTargetObjectiveWeights weights,
    @NonNull SugarAcidityTargetKind targetKind,
    @NonNull SugarAcidityTargetGeometryShape targetShape,
    @NonNull List<@NonNull ProfileEvaluationAxis> missingMeasurements
) {

  public ProfileTargetDistance {
    requireNonNull(status, "target distance status cannot be null");
    requireNonNull(point, "sugar-acidity point cannot be null");
    requireNonNull(nearestTargetPoint, "nearest target point cannot be null");
    requireNonNull(objectiveFunction, "objective function cannot be null");
    requireNonNull(metric, "distance metric cannot be null");
    requireNonNull(weights, "objective weights cannot be null");
    requireNonNull(targetKind, "target kind cannot be null");
    requireNonNull(targetShape, "target shape cannot be null");
    missingMeasurements = List.copyOf(requireNonNull(missingMeasurements, "missing measurements cannot be null"));
    if (status == ProfileTargetDistanceStatus.SCORED && value == null) {
      throw new DomainRuleViolationException("scored target distance value cannot be null");
    }
    if (status == ProfileTargetDistanceStatus.UNSCORABLE && missingMeasurements.isEmpty()) {
      throw new DomainRuleViolationException("unscorable target distance must contain missing measurements");
    }
    if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
      throw new DomainRuleViolationException("target distance value cannot be negative");
    }
  }

  public boolean scored() {
    return status == ProfileTargetDistanceStatus.SCORED;
  }
}
