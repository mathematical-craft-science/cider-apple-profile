package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract
    .FeedstockTargetDistanceView.EvaluationAxis;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Target-aware explanation of how a Feedstock source can contribute to a blend. */
public record FeedstockContributionView(
    @NonNull List<@NonNull Role> roles,
    @NonNull List<@NonNull Evidence> evidence,
    @NonNull Set<@NonNull EvaluationAxis> missingAxes
) {

  public FeedstockContributionView {
    roles = List.copyOf(requireNonNull(roles, "feedstock contribution roles cannot be null"));
    evidence = List.copyOf(requireNonNull(evidence, "feedstock contribution evidence cannot be null"));
    missingAxes = Set.copyOf(requireNonNull(missingAxes, "feedstock contribution missing axes cannot be null"));
    if (roles.isEmpty()) {
      throw new ApplicationValidationException("feedstock target contribution requires at least one role");
    }
  }

  public record Evidence(
      @NonNull EvaluationAxis axis,
      @NonNull BigDecimal sourceValue,
      @NonNull BigDecimal targetCoordinate,
      @NonNull BigDecimal deviationFromTarget,
      @NonNull Direction direction,
      @NonNull Effect effect,
      @NonNull Severity severity
  ) {

    public Evidence {
      requireNonNull(axis, "feedstock contribution axis cannot be null");
      requireNonNull(sourceValue, "feedstock contribution source value cannot be null");
      requireNonNull(targetCoordinate, "feedstock contribution target coordinate cannot be null");
      requireNonNull(deviationFromTarget, "feedstock contribution deviation cannot be null");
      requireNonNull(direction, "feedstock contribution direction cannot be null");
      requireNonNull(effect, "feedstock contribution effect cannot be null");
      requireNonNull(severity, "feedstock contribution severity cannot be null");
    }
  }

  public enum Role {
    BALANCED_BASE,
    SUGAR_CONTRIBUTOR,
    SUGAR_REDUCER,
    ACIDITY_CONTRIBUTOR,
    TANNIN_CONTRIBUTOR,
    ACIDITY_REDUCER,
    TANNIN_SOFTENER,
    DILUTION_SOURCE
  }

  public enum Direction { BELOW_TARGET, ON_TARGET, ABOVE_TARGET }

  public enum Effect {
    RAISES_SUGAR_POTENTIAL,
    LOWERS_SUGAR_POTENTIAL,
    RAISES_ACIDITY,
    SOFTENS_ACIDITY_HEAVY_BLEND,
    BUILDS_TANNIN_STRUCTURE,
    SOFTENS_TANNIN_HEAVY_BLEND,
    NO_DIRECTIONAL_EFFECT
  }

  public enum Severity { SUPPORTING, NEUTRAL }
}
