package org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.EnumMap;
import java.util.Map;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Normalizes axes by expert sensitivity units:
 * normalized(x) = x / unit, so distance delta is (x1 - x2) / unit.
 *
 * <p>This normalizer is intended for expert calibration. A unit represents one meaningful movement
 * on that axis, for example 0.005 SG or 1 g/L acidity. It is useful when a technologist wants
 * distance to reflect practical adjustment sensitivity rather than fixed axis ranges.
 */
public final class SensitivityUnitProfileAxisNormalizer implements ProfileAxisNormalizer {

  private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

  private final @NonNull Map<@NonNull ProfileEvaluationAxis, @NonNull BigDecimal> units;

  public SensitivityUnitProfileAxisNormalizer(
      @NonNull Map<@NonNull ProfileEvaluationAxis, @NonNull BigDecimal> units
  ) {
    requireNonNull(units, "axis sensitivity units cannot be null");
    final var unitMap = new EnumMap<ProfileEvaluationAxis, BigDecimal>(ProfileEvaluationAxis.class);
    units.forEach((axis, unit) -> {
      requireNonNull(axis, "profile evaluation axis cannot be null");
      requireNonNull(unit, "axis sensitivity unit cannot be null");
      if (unit.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("axis sensitivity unit must be positive: " + axis);
      }
      unitMap.put(axis, unit);
    });
    this.units = Map.copyOf(unitMap);
  }

  public static @NonNull SensitivityUnitProfileAxisNormalizer ciderBlendingDefaults() {
    return new SensitivityUnitProfileAxisNormalizer(Map.of(
        ProfileEvaluationAxis.SUGAR, new BigDecimal("0.005"),
        ProfileEvaluationAxis.ACIDITY, new BigDecimal("1.0"),
        ProfileEvaluationAxis.TANNIN, new BigDecimal("250")
    ));
  }

  @Override
  public @NonNull BigDecimal normalize(
      @NonNull ProfileEvaluationAxis axis,
      @NonNull BigDecimal value
  ) {
    requireNonNull(axis, "profile evaluation axis cannot be null");
    requireNonNull(value, "axis value cannot be null");
    final var unit = units.get(axis);
    if (unit == null) { // NOSONAR
      throw new IllegalArgumentException("No sensitivity unit configured for axis: " + axis);
    }
    return value.divide(unit, MATH_CONTEXT);
  }
}
