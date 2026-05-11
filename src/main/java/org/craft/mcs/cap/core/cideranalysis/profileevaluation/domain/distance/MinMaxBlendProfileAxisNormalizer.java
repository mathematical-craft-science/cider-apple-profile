package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Normalizes each blend profile axis by min-max scaling:
 * normalized(x) = (x - min) / (max - min).
 */
public final class MinMaxBlendProfileAxisNormalizer implements BlendProfileAxisNormalizer {

  private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

  private final @NonNull Map<@NonNull BlendProfileAxis, @NonNull BlendProfileAxisNormalizationRange> ranges;

  public MinMaxBlendProfileAxisNormalizer(
      @NonNull List<@NonNull BlendProfileAxisNormalizationRange> ranges
  ) {
    requireNonNull(ranges, "axis normalization ranges cannot be null");
    final var rangeMap = new EnumMap<BlendProfileAxis, BlendProfileAxisNormalizationRange>(
        BlendProfileAxis.class
    );
    ranges.forEach(range -> rangeMap.put(range.axis(), range));
    this.ranges = Map.copyOf(rangeMap);
  }

  public static @NonNull MinMaxBlendProfileAxisNormalizer fullCiderProfileRange() {
    return new MinMaxBlendProfileAxisNormalizer(BuiltInBlendProfileAxisRanges.fullRanges());
  }

  @Override
  public @NonNull BigDecimal normalize(
      @NonNull BlendProfileAxis axis,
      @NonNull BigDecimal value
  ) {
    requireNonNull(axis, "objective measurement axis cannot be null");
    requireNonNull(value, "axis value cannot be null");
    final var range = ranges.get(axis);
    if (range == null) { // NOSONAR
      throw new IllegalArgumentException("No normalization range configured for axis: " + axis);
    }
    return value.subtract(range.min())
        .divide(range.max().subtract(range.min()), MATH_CONTEXT);
  }
}
