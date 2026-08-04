package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization;

import java.math.BigDecimal;
import java.math.MathContext;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Normalizes axes by expert sensitivity units:
 * normalized(x) = x / unit, so distance delta is (x1 - x2) / unit.
 *
 * <p>This normalizer is intended for expert calibration. A unit represents one meaningful movement
 * on that axis, for example 0.005 SG or 1 g/L Titratable Acidity. It is useful when a
 * technologist wants distance to reflect practical adjustment sensitivity rather than fixed axis
 * ranges.
 */
public final class SensitivityUnitProfileAxisNormalizer implements ProfileAxisNormalizer {

  private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

  private final @NonNull ProfileAxisSensitivityUnits units;

  public SensitivityUnitProfileAxisNormalizer(
      @NonNull ProfileAxisSensitivityUnits units
  ) {
    this.units = requireNonNull(units, "axis sensitivity units cannot be null");
  }

  @Override
  public @NonNull BigDecimal normalize(
      @NonNull ProfileEvaluationAxis axis,
      @NonNull BigDecimal value
  ) {
    requireNonNull(axis, "profile evaluation axis cannot be null");
    requireNonNull(value, "axis value cannot be null");
    return value.divide(units.unitFor(axis), MATH_CONTEXT);
  }
}
