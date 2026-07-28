package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Normalizes axes by cider-making effective ranges:
 * normalized(x) = (x - effectiveMin) / (effectiveMax - effectiveMin).
 *
 * <p>This is the default normalizer for blending planning. It uses practical cider-making ranges
 * rather than absolute value-object limits, so distance reflects meaningful sensory/process
 * movement in typical source material.
 */
public final class EffectiveRangeProfileAxisNormalizer implements ProfileAxisNormalizer {

  private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

  private final @NonNull Map<@NonNull ProfileEvaluationAxis, @NonNull ProfileAxisNormalizationRange> ranges;

  public EffectiveRangeProfileAxisNormalizer(
      @NonNull List<@NonNull ProfileAxisNormalizationRange> ranges
  ) {
    requireNonNull(ranges, "effective normalization ranges cannot be null");
    final var rangeMap = new EnumMap<ProfileEvaluationAxis, ProfileAxisNormalizationRange>(
        ProfileEvaluationAxis.class
    );
    ranges.forEach(range -> rangeMap.put(range.axis(), range));
    this.ranges = Map.copyOf(rangeMap);
  }

  public static @NonNull EffectiveRangeProfileAxisNormalizer ciderBlendingDefaults() {
    return new EffectiveRangeProfileAxisNormalizer(
        BuiltInProfileAxisRanges.ciderBlendingEffectiveRangesV1()
    );
  }

  @Override
  public @NonNull BigDecimal normalize(
      @NonNull ProfileEvaluationAxis axis,
      @NonNull BigDecimal value
  ) {
    requireNonNull(axis, "profile evaluation axis cannot be null");
    requireNonNull(value, "axis value cannot be null");
    final var range = rangeFor(axis);
    return value.subtract(range.min())
        .divide(range.max().subtract(range.min()), MATH_CONTEXT);
  }

  private @NonNull ProfileAxisNormalizationRange rangeFor(@NonNull ProfileEvaluationAxis axis) {
    return Optional.ofNullable(ranges.get(axis))
        .orElseThrow(() -> new DomainRuleViolationException(
            "No effective normalization range configured for axis: " + axis
        ));
  }
}
