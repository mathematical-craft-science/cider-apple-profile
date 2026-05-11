package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.EnumMap;
import java.util.Map;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Normalizes axes by expert sensitivity units:
 * normalized(x) = x / unit, so distance delta is (x1 - x2) / unit.
 */
public final class SensitivityUnitBlendProfileAxisNormalizer implements BlendProfileAxisNormalizer {

  private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

  private final @NonNull Map<@NonNull BlendProfileAxis, @NonNull BigDecimal> units;

  public SensitivityUnitBlendProfileAxisNormalizer(
      @NonNull Map<@NonNull BlendProfileAxis, @NonNull BigDecimal> units
  ) {
    requireNonNull(units, "axis sensitivity units cannot be null");
    final var unitMap = new EnumMap<BlendProfileAxis, BigDecimal>(BlendProfileAxis.class);
    units.forEach((axis, unit) -> {
      requireNonNull(axis, "blend profile axis cannot be null");
      requireNonNull(unit, "axis sensitivity unit cannot be null");
      if (unit.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("axis sensitivity unit must be positive: " + axis);
      }
      unitMap.put(axis, unit);
    });
    this.units = Map.copyOf(unitMap);
  }

  public static @NonNull SensitivityUnitBlendProfileAxisNormalizer ciderBlendingDefaults() {
    return new SensitivityUnitBlendProfileAxisNormalizer(Map.of(
        BlendProfileAxis.SUGAR, new BigDecimal("0.005"),
        BlendProfileAxis.ACIDITY, new BigDecimal("1.0"),
        BlendProfileAxis.TANNIN, new BigDecimal("250")
    ));
  }

  @Override
  public @NonNull BigDecimal normalize(
      @NonNull BlendProfileAxis axis,
      @NonNull BigDecimal value
  ) {
    requireNonNull(axis, "blend profile axis cannot be null");
    requireNonNull(value, "axis value cannot be null");
    final var unit = units.get(axis);
    if (unit == null) { // NOSONAR
      throw new IllegalArgumentException("No sensitivity unit configured for axis: " + axis);
    }
    return value.divide(unit, MATH_CONTEXT);
  }
}
