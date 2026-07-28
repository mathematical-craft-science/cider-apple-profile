package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment;

import java.math.BigDecimal;
import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGeometryShape;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileDistanceMetric;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetKind;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isNegative;


/**
 * Result of scoring a profile point against a target objective.
 *
 * <p>The result keeps the original point, nearest target point, optional tannin values, selected
 * objective function, metric, weights, and missing axes so an assessment remains explainable
 * instead of exposing only a scalar score.</p>
 */
public record ProfileTargetDistance(
    @NonNull ProfileTargetDistanceStatus status,
    @NonNull SugarAcidityGraphPoint point,
    @NonNull SugarAcidityGraphPoint nearestTargetPoint,
    @Nullable TanninValue tannin,
    @Nullable TanninValue nearestTargetTannin,
    @Nullable BigDecimal value,
    boolean insideSugarAcidityTarget,
    boolean insideTarget,
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunction,
    @NonNull ProfileDistanceMetric metric,
    @NonNull ProfileTargetObjectiveWeights weights,
    @NonNull SugarAcidityTargetKind targetKind,
    @NonNull SugarAcidityGeometryShape targetShape,
    @NonNull Set<@NonNull ProfileEvaluationAxis> missingAxes
) {

  public ProfileTargetDistance {
    requireNonNull(status, "target distance status cannot be null");
    requireNonNull(point, "SG/TA point cannot be null");
    requireNonNull(nearestTargetPoint, "nearest target point cannot be null");
    requireNonNull(objectiveFunction, "objective function cannot be null");
    requireNonNull(metric, "distance metric cannot be null");
    requireNonNull(weights, "objective weights cannot be null");
    requireNonNull(targetKind, "target kind cannot be null");
    requireNonNull(targetShape, "target shape cannot be null");
    missingAxes = Set.copyOf(requireNonNull(missingAxes, "missing profile target axes cannot be null"));
    validateStatusState(status, value, insideTarget, missingAxes);
    if (insideTarget && !insideSugarAcidityTarget) {
      throw new DomainRuleViolationException(
          "profile cannot be inside the full target outside its SG/TA target"
      );
    }
    if ((tannin == null) != (nearestTargetTannin == null)) {
      throw new DomainRuleViolationException("candidate and nearest target tannin must be present together");
    }
    if (value != null && isNegative(value)) {
      throw new DomainRuleViolationException("target distance value cannot be negative");
    }
    if (insideTarget && value != null && value.signum() != 0) {
      throw new DomainRuleViolationException("profile inside the target must have zero distance");
    }
  }

  public boolean scored() {
    return status == ProfileTargetDistanceStatus.SCORED;
  }

  /** Returns the stable product-facing interpretation without changing the technical distance. */
  public @NonNull ProfileTargetFitAssessment fitAssessment() {
    return ProfileTargetFitPolicy.assess(this);
  }

  private static void validateStatusState(
      @NonNull ProfileTargetDistanceStatus status,
      @Nullable BigDecimal value,
      boolean insideTarget,
      @NonNull Set<@NonNull ProfileEvaluationAxis> missingAxes
  ) {
    if (status == ProfileTargetDistanceStatus.SCORED) {
      if (value == null) {
        throw new DomainRuleViolationException("scored target distance value cannot be null");
      }
      if (!missingAxes.isEmpty()) {
        throw new DomainRuleViolationException("scored target distance cannot contain missing axes");
      }
      return;
    }

    if (value != null) {
      throw new DomainRuleViolationException("unscorable target distance value must be null");
    }
    if (missingAxes.isEmpty()) {
      throw new DomainRuleViolationException("unscorable target distance must contain missing axes");
    }
    if (insideTarget) {
      throw new DomainRuleViolationException("unscorable target distance cannot be inside a target");
    }
  }
}
