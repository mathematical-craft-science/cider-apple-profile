package org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.CiderAppleClassificationInput;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.ClassificationCharacteristic;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.ClassificationIntervalPolicy;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisRange;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Calculates the North American cider profile levels from typed canonical characteristics.
 *
 * <p>{@link #calculate(CiderAppleClassificationInput)} requires Specific Gravity (SG), Titratable
 * Acidity (TA), and tannin. Focused methods can assess one present characteristic for projections
 * such as graph-point tannin styling. The calculation maps SG to a sugar level and TA to an acidity
 * level. These are cider-apple classification concepts, not sensory sweetness or perceived-acidity
 * assessments.</p>
 */
public final class NorthAmericanProfileCalculator {

  private final @NonNull NorthAmericanThresholds thresholds;

  public NorthAmericanProfileCalculator(@NonNull NorthAmericanThresholds thresholds) {
    this.thresholds = requireNonNull(thresholds);
  }

  private static @NonNull NorthAmericanTier tierOf(
      @NonNull BigDecimal value,
      @NonNull AxisRange low,
      @NonNull AxisRange medium,
      @NonNull AxisRange high,
      @NonNull AxisRange veryHigh
  ) {
    requireNonNull(value, "axis value cannot be null");
    requireNonNull(low, "low range cannot be null");
    requireNonNull(medium, "medium range cannot be null");
    requireNonNull(high, "high range cannot be null");
    requireNonNull(veryHigh, "very-high range cannot be null");

    if (low.contains(value)) {
      return NorthAmericanTier.LOW;
    }
    if (medium.contains(value)) {
      return NorthAmericanTier.MEDIUM;
    }
    if (high.contains(value)) {
      return NorthAmericanTier.HIGH;
    }
    if (veryHigh.contains(value)) {
      return NorthAmericanTier.VERY_HIGH;
    }

    throw new DomainRuleViolationException("value does not fit any North American profile range: " + value);
  }

  private static @NonNull BigDecimal value(
      @NonNull CiderAppleClassificationInput input,
      @NonNull ClassificationCharacteristic characteristic
  ) {
    if (!input.has(characteristic)) {
      throw new DomainRuleViolationException(
          "missing North American profile characteristic: " + characteristic
      );
    }
    return input.representativeValue(
        characteristic,
        ClassificationIntervalPolicy.MIDPOINT_WITH_BOUNDARY_EVIDENCE
    );
  }

  /**
   * Assesses sugar content from SG using only the North American profile ranges.
   */
  public @NonNull NorthAmericanSugarLevel assessSugar(
      @NonNull CiderAppleClassificationInput input
  ) {
    requireNonNull(input, "cider apple classification input cannot be null");
    return switch (tierOf(
        value(input, ClassificationCharacteristic.SUGAR_CONTENT),
        thresholds.lowSugar(),
        thresholds.mediumSugar(),
        thresholds.highSugar(),
        thresholds.veryHighSugar()
    )) {
      case LOW -> NorthAmericanSugarLevel.LOW;
      case MEDIUM -> NorthAmericanSugarLevel.MEDIUM;
      case HIGH -> NorthAmericanSugarLevel.HIGH;
      case VERY_HIGH -> NorthAmericanSugarLevel.VERY_HIGH;
    };
  }

  /**
   * Assesses acidity from TA using only the North American profile ranges.
   */
  public @NonNull NorthAmericanAcidityLevel assessAcidity(
      @NonNull CiderAppleClassificationInput input
  ) {
    requireNonNull(input, "cider apple classification input cannot be null");
    return switch (tierOf(
        value(input, ClassificationCharacteristic.ACIDITY),
        thresholds.lowAcidity(),
        thresholds.mediumAcidity(),
        thresholds.highAcidity(),
        thresholds.veryHighAcidity()
    )) {
      case LOW -> NorthAmericanAcidityLevel.LOW;
      case MEDIUM -> NorthAmericanAcidityLevel.MEDIUM;
      case HIGH -> NorthAmericanAcidityLevel.HIGH;
      case VERY_HIGH -> NorthAmericanAcidityLevel.VERY_HIGH;
    };
  }

  /**
   * Assesses tannin using the three-level North American tannin scale.
   */
  public @NonNull NorthAmericanTanninLevel assessTannin(
      @NonNull CiderAppleClassificationInput input
  ) {
    requireNonNull(input, "cider apple classification input cannot be null");
    final var tannin = value(input, ClassificationCharacteristic.TANNIN);
    if (thresholds.lowTannin().contains(tannin)) {
      return NorthAmericanTanninLevel.LOW;
    }
    if (thresholds.mediumTannin().contains(tannin)) {
      return NorthAmericanTanninLevel.MEDIUM;
    }
    if (thresholds.highTannin().contains(tannin)) {
      return NorthAmericanTanninLevel.HIGH;
    }
    throw new DomainRuleViolationException(
        "tannin does not fit any North American profile range: " + tannin
    );
  }

  public @NonNull NorthAmericanProfile calculate(@NonNull CiderAppleClassificationInput input) {
    requireNonNull(input, "cider apple classification input cannot be null");

    return new NorthAmericanProfile(
        assessSugar(input),
        assessAcidity(input),
        assessTannin(input)
    );
  }

  private enum NorthAmericanTier {
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH
  }
}
