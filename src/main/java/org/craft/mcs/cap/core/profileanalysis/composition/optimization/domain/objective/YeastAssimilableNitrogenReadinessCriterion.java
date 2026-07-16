package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective;

import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.InvalidChemicalProfileCompositionOptimizationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileComposition;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isGreaterThan;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isLessThan;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isPositive;


/**
 * Adds a fermentation-start readiness penalty when blended YAN is missing or outside the configured
 * range.
 *
 * <p>YAN is not part of profile-target distance because it is not a sugar/acidity/tannin target
 * axis. It is a readiness criterion: a candidate can be close to the desired target profile but
 * still rank lower if YAN suggests a risky fermentation start.
 */
public record YeastAssimilableNitrogenReadinessCriterion(
    @Nullable YeastAssimilableNitrogenValue minimum,
    @Nullable YeastAssimilableNitrogenValue maximum,
    @NonNull BigDecimal weight,
    @NonNull CompositionOptimizationMissingPolicy missingPolicy,
    @NonNull BigDecimal missingPenalty
) implements CompositionOptimizationCriterion {

  private static final int PENALTY_SCALE = 9;

  public YeastAssimilableNitrogenReadinessCriterion {
    requireNonNull(weight, "YAN readiness criterion weight cannot be null");
    requireNonNull(missingPolicy, "YAN readiness missing policy cannot be null");
    requireNonNull(missingPenalty, "YAN readiness missing penalty cannot be null");

    if (minimum == null && maximum == null) {
      throw new InvalidChemicalProfileCompositionOptimizationException(
          "YAN readiness criterion requires minimum or maximum value"
      );
    }
    if (!isPositive(weight)) {
      throw new InvalidChemicalProfileCompositionOptimizationException(
          "YAN readiness criterion weight must be positive"
      );
    }
    if (minimum != null && !isPositive(minimum.asBigDecimal())) {
      throw new InvalidChemicalProfileCompositionOptimizationException("YAN readiness minimum must be positive");
    }
    if (maximum != null && !isPositive(maximum.asBigDecimal())) {
      throw new InvalidChemicalProfileCompositionOptimizationException("YAN readiness maximum must be positive");
    }
    if (minimum != null && maximum != null && minimum.isGreaterThan(maximum)) {
      throw new InvalidChemicalProfileCompositionOptimizationException("YAN readiness minimum cannot exceed maximum");
    }
    if (isLessThan(missingPenalty, BigDecimal.ZERO)) {
      throw new InvalidChemicalProfileCompositionOptimizationException(
          "YAN readiness missing penalty cannot be negative"
      );
    }
  }

  @Override
  public @NonNull CompositionOptimizationCriterionAssessment assess(
      @NonNull ChemicalProfileComposition composition
  ) {
    requireNonNull(composition, "chemical profile composition cannot be null");

    final var yan = composition.yeastAssimilableNitrogen();
    if (yan == null) {
      return missingAssessment();
    }

    final var value = yan.value();
    final var minimumValue = minimumValue();
    if (minimumValue != null && isLessThan(value, minimumValue)) {
      return CompositionOptimizationCriterionAssessment.withPenalty(new CompositionOptimizationPenalty(
          CompositionOptimizationPenaltyCode.LOW_YEAST_ASSIMILABLE_NITROGEN,
          CompositionOptimizationPenaltySeverity.WARNING,
          proportionalPenalty(minimumValue.subtract(value), minimumValue),
          ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN,
          value,
          minimumValue
      ));
    }
    final var maximumValue = maximumValue();
    if (maximumValue != null && isGreaterThan(value, maximumValue)) {
      return CompositionOptimizationCriterionAssessment.withPenalty(new CompositionOptimizationPenalty(
          CompositionOptimizationPenaltyCode.HIGH_YEAST_ASSIMILABLE_NITROGEN,
          CompositionOptimizationPenaltySeverity.WARNING,
          proportionalPenalty(value.subtract(maximumValue), maximumValue),
          ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN,
          value,
          maximumValue
      ));
    }

    return CompositionOptimizationCriterionAssessment.allowed();
  }

  private @NonNull CompositionOptimizationCriterionAssessment missingAssessment() {
    return switch (missingPolicy) {
      case IGNORE_IF_MISSING -> CompositionOptimizationCriterionAssessment.allowed();
      case PENALIZE_IF_MISSING -> CompositionOptimizationCriterionAssessment.withPenalty(
          new CompositionOptimizationPenalty(
          CompositionOptimizationPenaltyCode.MISSING_YEAST_ASSIMILABLE_NITROGEN,
          CompositionOptimizationPenaltySeverity.WARNING,
          missingPenalty.multiply(weight),
          ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN,
          null,
          minimumValue()
          )
      );
      case EXCLUDE_IF_MISSING -> CompositionOptimizationCriterionAssessment.excluded();
    };
  }

  private @Nullable BigDecimal minimumValue() {
    return minimum == null ? null : minimum.milligramsNitrogenPerLiter();
  }

  private @Nullable BigDecimal maximumValue() {
    return maximum == null ? null : maximum.milligramsNitrogenPerLiter();
  }

  private @NonNull BigDecimal proportionalPenalty(
      @NonNull BigDecimal gap,
      @NonNull BigDecimal threshold
  ) {
    return gap
        .divide(threshold, PENALTY_SCALE, RoundingMode.HALF_UP)
        .multiply(weight);
  }
}
