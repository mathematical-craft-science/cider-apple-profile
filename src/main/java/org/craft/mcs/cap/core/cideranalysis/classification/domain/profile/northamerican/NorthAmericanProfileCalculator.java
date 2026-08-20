package org.craft.mcs.cap.core.cideranalysis.classification.domain.profile.northamerican;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.engine.CiderAppleClassificationInput;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.engine.ClassificationIntervalPolicy;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.rule.ClassificationValueRange;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
      @NonNull ClassificationValueRange low,
      @NonNull ClassificationValueRange medium,
      @NonNull ClassificationValueRange high,
      @NonNull ClassificationValueRange veryHigh
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

  private static <T extends MeasuredValue> @NonNull BigDecimal value(
      @NonNull CharacteristicValue<T> characteristic
  ) {
    return ClassificationIntervalPolicy.MIDPOINT_WITH_BOUNDARY_EVIDENCE.representativeValue(
        requireNonNull(characteristic, "North American profile characteristic cannot be null")
    );
  }

  /**
   * Assesses sugar content from SG using only the North American profile ranges.
   */
  public @NonNull NorthAmericanSugarLevel assessSugar(
      @NonNull CharacteristicValue<SpecificGravityValue> sugarContent
  ) {
    return switch (tierOf(
        value(sugarContent),
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
      @NonNull CharacteristicValue<TitratableAcidityValue> acidity
  ) {
    return switch (tierOf(
        value(acidity),
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
      @NonNull CharacteristicValue<TanninValue> tannin
  ) {
    final var representativeValue = value(tannin);
    if (thresholds.lowTannin().contains(representativeValue)) {
      return NorthAmericanTanninLevel.LOW;
    }
    if (thresholds.mediumTannin().contains(representativeValue)) {
      return NorthAmericanTanninLevel.MEDIUM;
    }
    if (thresholds.highTannin().contains(representativeValue)) {
      return NorthAmericanTanninLevel.HIGH;
    }
    throw new DomainRuleViolationException(
        "tannin does not fit any North American profile range: " + representativeValue
    );
  }

  public @NonNull NorthAmericanProfile calculate(@NonNull CiderAppleClassificationInput input) {
    requireNonNull(input, "cider apple classification input cannot be null");

    return new NorthAmericanProfile(
        assessSugar(requireCharacteristic(input.sugarContent(), "sugar content")),
        assessAcidity(requireCharacteristic(input.acidity(), "acidity")),
        assessTannin(requireCharacteristic(input.tannin(), "tannin"))
    );
  }

  private static <T extends MeasuredValue> @NonNull CharacteristicValue<T> requireCharacteristic(
      @Nullable CharacteristicValue<T> characteristic,
      @NonNull String name
  ) {
    if (characteristic == null) {
      throw new DomainRuleViolationException("missing North American profile characteristic: " + name);
    }
    return characteristic;
  }

  private enum NorthAmericanTier {
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH
  }
}
