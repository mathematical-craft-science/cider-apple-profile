package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.contribution;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileEvaluationAxis;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRangePosition;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Per-axis explanation for a target-aware contribution role.
 *
 * <p>{@code targetCoordinate} is the nearest coordinate on the target geometry for the source
 * point. {@code targetPosition} follows the target's membership rules, so a value equal to an
 * exclusive upper bound is above the target despite having zero numeric deviation from its
 * closure.
 */
public record CompositionContributionEvidence(
    @NonNull ProfileEvaluationAxis axis,
    @NonNull MeasuredValue sourceValue,
    @NonNull MeasuredValue targetCoordinate,
    @NonNull MeasuredValueRangePosition targetPosition
) {

  public CompositionContributionEvidence {
    requireNonNull(axis, "composition contribution evidence axis cannot be null");
    requireNonNull(sourceValue, "composition contribution evidence source value cannot be null");
    requireNonNull(targetCoordinate, "composition contribution evidence target coordinate cannot be null");
    requireNonNull(targetPosition, "composition contribution evidence target position cannot be null");
    if (!axis.supports(sourceValue) || !axis.supports(targetCoordinate)) {
      throw new DomainRuleViolationException(
          "composition contribution evidence values must match their evaluation axis"
      );
    }
    final var deviationSign = sourceValue.asBigDecimal().compareTo(targetCoordinate.asBigDecimal());
    final var positionIsConsistent = switch (targetPosition) {
      case BELOW -> deviationSign < 0;
      case WITHIN -> deviationSign == 0;
      case ABOVE -> deviationSign >= 0;
    };
    if (!positionIsConsistent) {
      throw new DomainRuleViolationException(
          "composition contribution target position contradicts its numeric deviation"
      );
    }
  }

  public @NonNull BigDecimal deviationFromTarget() {
    return sourceValue.asBigDecimal().subtract(targetCoordinate.asBigDecimal());
  }

  public @NonNull CompositionContributionEvidenceDirection direction() {
    return switch (targetPosition) {
      case BELOW -> CompositionContributionEvidenceDirection.BELOW_TARGET;
      case WITHIN -> CompositionContributionEvidenceDirection.ON_TARGET;
      case ABOVE -> CompositionContributionEvidenceDirection.ABOVE_TARGET;
    };
  }

  public @NonNull CompositionContributionEvidenceEffect effect() {
    final var direction = direction();
    if (direction == CompositionContributionEvidenceDirection.ON_TARGET) {
      return CompositionContributionEvidenceEffect.NO_DIRECTIONAL_EFFECT;
    }
    return switch (axis) {
      case SPECIFIC_GRAVITY -> direction == CompositionContributionEvidenceDirection.ABOVE_TARGET
          ? CompositionContributionEvidenceEffect.RAISES_SUGAR_POTENTIAL
          : CompositionContributionEvidenceEffect.LOWERS_SUGAR_POTENTIAL;
      case TITRATABLE_ACIDITY -> direction == CompositionContributionEvidenceDirection.ABOVE_TARGET
          ? CompositionContributionEvidenceEffect.RAISES_ACIDITY
          : CompositionContributionEvidenceEffect.SOFTENS_ACIDITY_HEAVY_BLEND;
      case TANNIN -> direction == CompositionContributionEvidenceDirection.ABOVE_TARGET
          ? CompositionContributionEvidenceEffect.BUILDS_TANNIN_STRUCTURE
          : CompositionContributionEvidenceEffect.SOFTENS_TANNIN_HEAVY_BLEND;
    };
  }

  public @NonNull CompositionContributionEvidenceSeverity severity() {
    return direction() == CompositionContributionEvidenceDirection.ON_TARGET
        ? CompositionContributionEvidenceSeverity.NEUTRAL
        : CompositionContributionEvidenceSeverity.SUPPORTING;
  }
}
