package org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitScoringConfiguration.ObjectiveFunction;
import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitScoringConfiguration.ObjectiveWeights;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isNegative;


/** Immutable target-fit facts published without exposing scoring domain objects. */
public record TargetFitCandidateScoresView(
    @NonNull TargetFitScoringConfiguration configuration,
    @NonNull List<@NonNull CandidateScore> scores
) {

  public TargetFitCandidateScoresView {
    requireNonNull(configuration, "target scoring configuration cannot be null");
    scores = List.copyOf(requireNonNull(scores, "target candidate scores cannot be null"));
    final var distinctReferences = scores.stream()
        .map(CandidateScore::reference)
        .distinct()
        .count();
    if (distinctReferences != scores.size()) {
      throw new ApplicationValidationException("target candidate score references must be unique");
    }
  }

  public record CandidateScore(
      @NonNull TargetFitCandidateReference reference,
      @NonNull Distance distance,
      @NonNull Contribution contribution
  ) {

    public CandidateScore {
      requireNonNull(reference, "target candidate reference cannot be null");
      requireNonNull(distance, "target distance cannot be null");
      requireNonNull(contribution, "target contribution cannot be null");
    }
  }

  public record Distance(
      @NonNull DistanceStatus status,
      @NonNull Coordinates point,
      @NonNull Coordinates nearestTargetPoint,
      @NonNull TargetPosition targetPosition,
      @Nullable TanninValue tannin,
      @Nullable TanninValue nearestTargetTannin,
      @Nullable BigDecimal value,
      @Nullable Integer matchPercent,
      @NonNull FitStatus fitStatus,
      boolean insideSugarAcidityTarget,
      boolean insideTarget,
      @NonNull ObjectiveFunction objectiveFunction,
      @NonNull DistanceMetric metric,
      @NonNull ObjectiveWeights weights,
      @NonNull TargetKind targetKind,
      @NonNull GeometryShape targetShape,
      @NonNull Set<@NonNull EvaluationAxis> missingAxes
  ) {

    public Distance {
      requireNonNull(status, "target distance status cannot be null");
      requireNonNull(point, "target distance point cannot be null");
      requireNonNull(nearestTargetPoint, "nearest target point cannot be null");
      requireNonNull(targetPosition, "target position cannot be null");
      requireNonNull(fitStatus, "target fit status cannot be null");
      requireNonNull(objectiveFunction, "target objective function cannot be null");
      requireNonNull(metric, "target distance metric cannot be null");
      requireNonNull(weights, "target objective weights cannot be null");
      requireNonNull(targetKind, "target kind cannot be null");
      requireNonNull(targetShape, "target shape cannot be null");
      missingAxes = Set.copyOf(requireNonNull(missingAxes, "missing target axes cannot be null"));
      validateStatus(status, value, matchPercent, fitStatus, insideTarget, missingAxes);
      if (insideTarget && !insideSugarAcidityTarget) {
        throw new ApplicationValidationException("full target containment requires SG/TA containment");
      }
      if (insideSugarAcidityTarget != targetPosition.inside()) {
        throw new ApplicationValidationException("SG/TA containment must agree with target position");
      }
      if ((tannin == null) != (nearestTargetTannin == null)) {
        throw new ApplicationValidationException("candidate and nearest target tannin must be present together");
      }
      if (value != null && isNegative(value)) {
        throw new ApplicationValidationException("target distance cannot be negative");
      }
      if (insideTarget && (value == null || value.signum() != 0 || !Integer.valueOf(100).equals(matchPercent))) {
        throw new ApplicationValidationException("a point inside the target must have zero distance and 100% fit");
      }
    }

    private static void validateStatus(
        @NonNull DistanceStatus status,
        @Nullable BigDecimal value,
        @Nullable Integer matchPercent,
        @NonNull FitStatus fitStatus,
        boolean insideTarget,
        @NonNull Set<@NonNull EvaluationAxis> missingAxes
    ) {
      if (matchPercent != null && (matchPercent < 0 || matchPercent > 100)) {
        throw new ApplicationValidationException("target match percent must be between 0 and 100");
      }
      if (status == DistanceStatus.SCORED) {
        if (value == null || matchPercent == null || fitStatus == FitStatus.UNSCORABLE || !missingAxes.isEmpty()) {
          throw new ApplicationValidationException("scored target distance requires complete scoring facts");
        }
        return;
      }
      if (value != null || matchPercent != null || fitStatus != FitStatus.UNSCORABLE
          || missingAxes.isEmpty() || insideTarget) {
        throw new ApplicationValidationException("unscorable target distance requires missing axes only");
      }
    }
  }

  public record TargetPosition(
      @NonNull AxisPosition specificGravity,
      @NonNull AxisPosition titratableAcidity
  ) {

    public TargetPosition {
      requireNonNull(specificGravity, "specific gravity target position cannot be null");
      requireNonNull(titratableAcidity, "titratable acidity target position cannot be null");
    }

    public boolean inside() {
      return specificGravity == AxisPosition.WITHIN && titratableAcidity == AxisPosition.WITHIN;
    }
  }

  public record Coordinates(
      @NonNull SpecificGravityValue specificGravity,
      @NonNull TitratableAcidityValue titratableAcidity
  ) {

    public Coordinates {
      requireNonNull(specificGravity, "target coordinate specific gravity cannot be null");
      requireNonNull(titratableAcidity, "target coordinate titratable acidity cannot be null");
    }
  }

  public record Contribution(
      @NonNull List<@NonNull ContributionRole> roles,
      @NonNull List<@NonNull ContributionEvidence> evidence,
      @NonNull Set<@NonNull EvaluationAxis> missingAxes
  ) {

    public Contribution {
      roles = List.copyOf(requireNonNull(roles, "contribution roles cannot be null"));
      evidence = List.copyOf(requireNonNull(evidence, "contribution evidence cannot be null"));
      missingAxes = Set.copyOf(requireNonNull(missingAxes, "contribution missing axes cannot be null"));
      if (roles.isEmpty()) {
        throw new ApplicationValidationException("target contribution requires at least one role");
      }
    }

    public @NonNull ContributionRole primaryRole() {
      return roles.getFirst();
    }
  }

  public record ContributionEvidence(
      @NonNull EvaluationAxis axis,
      @NonNull BigDecimal sourceValue,
      @NonNull BigDecimal targetCoordinate,
      @NonNull BigDecimal deviationFromTarget,
      @NonNull EvidenceDirection direction,
      @NonNull EvidenceEffect effect,
      @NonNull EvidenceSeverity severity
  ) {

    public ContributionEvidence {
      requireNonNull(axis, "contribution evidence axis cannot be null");
      requireNonNull(sourceValue, "contribution source value cannot be null");
      requireNonNull(targetCoordinate, "contribution target coordinate cannot be null");
      requireNonNull(deviationFromTarget, "contribution deviation cannot be null");
      requireNonNull(direction, "contribution direction cannot be null");
      requireNonNull(effect, "contribution effect cannot be null");
      requireNonNull(severity, "contribution severity cannot be null");
    }
  }

  public enum DistanceStatus { SCORED, UNSCORABLE }
  public enum FitStatus { EXCELLENT, CLOSE, NEEDS_ADJUSTMENT, UNSCORABLE }
  public enum DistanceMetric { NORMALIZED_WEIGHTED_EUCLIDEAN, NORMALIZED_WEIGHTED_MANHATTAN }
  public enum TargetKind { PREDEFINED_ZONE, CUSTOM_RECTANGLE }
  public enum GeometryShape { RECTANGLE, ELLIPSE }
  public enum EvaluationAxis { SPECIFIC_GRAVITY, TITRATABLE_ACIDITY, TANNIN }
  public enum AxisPosition { BELOW, WITHIN, ABOVE }

  public enum ContributionRole {
    BALANCED_BASE,
    SUGAR_CONTRIBUTOR,
    SUGAR_REDUCER,
    ACIDITY_CONTRIBUTOR,
    TANNIN_CONTRIBUTOR,
    ACIDITY_REDUCER,
    TANNIN_SOFTENER,
    DILUTION_SOURCE
  }

  public enum EvidenceDirection { BELOW_TARGET, ON_TARGET, ABOVE_TARGET }

  public enum EvidenceEffect {
    RAISES_SUGAR_POTENTIAL,
    LOWERS_SUGAR_POTENTIAL,
    RAISES_ACIDITY,
    SOFTENS_ACIDITY_HEAVY_BLEND,
    BUILDS_TANNIN_STRUCTURE,
    SOFTENS_TANNIN_HEAVY_BLEND,
    NO_DIRECTIONAL_EFFECT
  }

  public enum EvidenceSeverity { SUPPORTING, NEUTRAL }
}
