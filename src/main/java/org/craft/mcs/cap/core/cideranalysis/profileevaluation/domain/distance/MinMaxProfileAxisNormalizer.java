package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Normalizes each profile evaluation axis by min-max scaling:
 * normalized(x) = (x - min) / (max - min).
 */
public final class MinMaxProfileAxisNormalizer implements ProfileAxisNormalizer {

  private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

  private final @NonNull Map<@NonNull ProfileEvaluationAxis, @NonNull ProfileAxisNormalizationRange> ranges;

  public MinMaxProfileAxisNormalizer(
      @NonNull List<@NonNull ProfileAxisNormalizationRange> ranges
  ) {
    requireNonNull(ranges, "axis normalization ranges cannot be null");
    final var rangeMap = new EnumMap<ProfileEvaluationAxis, ProfileAxisNormalizationRange>(
        ProfileEvaluationAxis.class
    );
    ranges.forEach(range -> rangeMap.put(range.axis(), range));
    this.ranges = Map.copyOf(rangeMap);
  }

  public static @NonNull MinMaxProfileAxisNormalizer fullCiderProfileRange() {
    return new MinMaxProfileAxisNormalizer(BuiltInProfileAxisRanges.fullRanges());
  }

  @Override
  public @NonNull BigDecimal normalize(
      @NonNull ProfileEvaluationAxis axis,
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
