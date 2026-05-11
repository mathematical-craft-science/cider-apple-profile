package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Normalizes axes by cider-making effective ranges:
 * normalized(x) = (x - effectiveMin) / (effectiveMax - effectiveMin).
 */
public final class EffectiveRangeBlendProfileAxisNormalizer implements BlendProfileAxisNormalizer {

  private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

  private final @NonNull Map<@NonNull BlendProfileAxis, @NonNull BlendProfileAxisNormalizationRange> ranges;

  public EffectiveRangeBlendProfileAxisNormalizer(
      @NonNull List<@NonNull BlendProfileAxisNormalizationRange> ranges
  ) {
    requireNonNull(ranges, "effective normalization ranges cannot be null");
    final var rangeMap = new EnumMap<BlendProfileAxis, BlendProfileAxisNormalizationRange>(
        BlendProfileAxis.class
    );
    ranges.forEach(range -> rangeMap.put(range.axis(), range));
    this.ranges = Map.copyOf(rangeMap);
  }

  public static @NonNull EffectiveRangeBlendProfileAxisNormalizer ciderBlendingDefaults() {
    return new EffectiveRangeBlendProfileAxisNormalizer(List.of(
        new BlendProfileAxisNormalizationRange(
            BlendProfileAxis.SUGAR,
            new BigDecimal("1.035"),
            new BigDecimal("1.075")
        ),
        new BlendProfileAxisNormalizationRange(
            BlendProfileAxis.ACIDITY,
            new BigDecimal("2.0"),
            new BigDecimal("9.0")
        ),
        new BlendProfileAxisNormalizationRange(
            BlendProfileAxis.TANNIN,
            new BigDecimal("500"),
            new BigDecimal("3500")
        )
    ));
  }

  @Override
  public @NonNull BigDecimal normalize(
      @NonNull BlendProfileAxis axis,
      @NonNull BigDecimal value
  ) {
    requireNonNull(axis, "blend profile axis cannot be null");
    requireNonNull(value, "axis value cannot be null");
    final var range = ranges.get(axis);
    if (range == null) { // NOSONAR
      throw new IllegalArgumentException("No effective normalization range configured for axis: " + axis);
    }
    return value.subtract(range.min())
        .divide(range.max().subtract(range.min()), MATH_CONTEXT);
  }
}
