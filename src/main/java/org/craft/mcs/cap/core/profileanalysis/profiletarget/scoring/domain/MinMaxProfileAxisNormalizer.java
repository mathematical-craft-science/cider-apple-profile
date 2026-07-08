package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Normalizes each profile evaluation axis by min-max scaling:
 * normalized(x) = (x - min) / (max - min).
 *
 * <p>This normalizer is useful for graph exploration and broad catalog comparison where the full
 * configured axis span should map to a common 0..1-ish scale.
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
    final var range = rangeFor(axis);
    return value.subtract(range.min())
        .divide(range.max().subtract(range.min()), MATH_CONTEXT);
  }

  private @NonNull ProfileAxisNormalizationRange rangeFor(@NonNull ProfileEvaluationAxis axis) {
    return Optional.ofNullable(ranges.get(axis))
        .orElseThrow(() -> new DomainRuleViolationException("No normalization range configured for axis: " + axis));
  }
}
