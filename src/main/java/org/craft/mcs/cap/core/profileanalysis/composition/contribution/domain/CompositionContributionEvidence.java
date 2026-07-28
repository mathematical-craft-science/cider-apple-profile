package org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Per-axis explanation for a target-aware contribution role.
 *
 * <p>{@code targetCoordinate} is the nearest coordinate on the target geometry for the source
 * point. Positive {@code deviationFromTarget} means the source is above that target coordinate;
 * negative means it is below it.
 */
public record CompositionContributionEvidence(
    @NonNull ProfileEvaluationAxis axis,
    @NonNull BigDecimal sourceValue,
    @NonNull BigDecimal targetCoordinate
) {

  public CompositionContributionEvidence {
    requireNonNull(axis, "composition contribution evidence axis cannot be null");
    requireNonNull(sourceValue, "composition contribution evidence source value cannot be null");
    requireNonNull(targetCoordinate, "composition contribution evidence target coordinate cannot be null");
  }

  public @NonNull BigDecimal deviationFromTarget() {
    return sourceValue.subtract(targetCoordinate);
  }

  public @NonNull CompositionContributionEvidenceDirection direction() {
    return switch (deviationFromTarget().signum()) {
      case -1 -> CompositionContributionEvidenceDirection.BELOW_TARGET;
      case 0 -> CompositionContributionEvidenceDirection.ON_TARGET;
      case 1 -> CompositionContributionEvidenceDirection.ABOVE_TARGET;
      default -> throw new DomainRuleViolationException("unexpected decimal signum");
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
