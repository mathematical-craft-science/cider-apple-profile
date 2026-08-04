package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isPositive;


/**
 * Meaningful canonical change represented by one normalized unit on each scoring axis.
 *
 * <p>These values calibrate distance sensitivity. They are neither measurement limits nor
 * cider-apple classification thresholds.</p>
 */
@ValueObject
public record ProfileAxisSensitivityUnits(
    @NonNull BigDecimal specificGravity,
    @NonNull BigDecimal titratableAcidity,
    @NonNull BigDecimal tannin
) {

  private static final BigDecimal CIDER_BLENDING_V1_SPECIFIC_GRAVITY_CHANGE =
      new BigDecimal("0.005");
  private static final BigDecimal CIDER_BLENDING_V1_TITRATABLE_ACIDITY_CHANGE =
      new BigDecimal("1.0");
  private static final BigDecimal CIDER_BLENDING_V1_TANNIN_CHANGE =
      new BigDecimal("250");

  public ProfileAxisSensitivityUnits {
    requirePositive(specificGravity, ProfileEvaluationAxis.SPECIFIC_GRAVITY);
    requirePositive(titratableAcidity, ProfileEvaluationAxis.TITRATABLE_ACIDITY);
    requirePositive(tannin, ProfileEvaluationAxis.TANNIN);
  }

  /**
   * Returns CAP's first operational cider-blending sensitivity calibration.
   *
   * <p>One unit means 0.005 SG, 1.0 g/L TA as malic acid, or 250 mg/L tannin as TAE.
   * The versioned name prevents silent reinterpretation if later calibration evidence changes.</p>
   */
  public static @NonNull ProfileAxisSensitivityUnits ciderBlendingV1() {
    return new ProfileAxisSensitivityUnits(
        CIDER_BLENDING_V1_SPECIFIC_GRAVITY_CHANGE,
        CIDER_BLENDING_V1_TITRATABLE_ACIDITY_CHANGE,
        CIDER_BLENDING_V1_TANNIN_CHANGE
    );
  }

  public @NonNull BigDecimal unitFor(@NonNull ProfileEvaluationAxis axis) {
    return switch (requireNonNull(axis, "profile evaluation axis cannot be null")) {
      case SPECIFIC_GRAVITY -> specificGravity;
      case TITRATABLE_ACIDITY -> titratableAcidity;
      case TANNIN -> tannin;
    };
  }

  private static void requirePositive(
      @NonNull BigDecimal value,
      @NonNull ProfileEvaluationAxis axis
  ) {
    requireNonNull(value, "axis sensitivity unit cannot be null");
    if (!isPositive(value)) {
      throw new DomainRuleViolationException("axis sensitivity unit must be positive: " + axis);
    }
  }
}
