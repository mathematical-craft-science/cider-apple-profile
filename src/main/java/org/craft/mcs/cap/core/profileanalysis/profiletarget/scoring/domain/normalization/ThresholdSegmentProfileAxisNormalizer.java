package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.EnumMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isAtLeast;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isLessThan;


/**
 * Normalizes axes piecewise by scoring-calibration breakpoints:
 * normalized(x) = (segmentIndex + (x - segmentMin) / (segmentMax - segmentMin)) / segmentCount.
 *
 * <p>This normalizer is useful for target-zone matching. It makes movement across domain
 * calibrated breakpoints more explicit than raw min-max scaling. The breakpoints belong to this
 * distance policy and must not be interpreted as a selected cider-apple classification system.
 */
public final class ThresholdSegmentProfileAxisNormalizer implements ProfileAxisNormalizer {

  private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

  private final @NonNull Map<@NonNull ProfileEvaluationAxis, @NonNull List<@NonNull BigDecimal>> breakpoints;

  public ThresholdSegmentProfileAxisNormalizer(
      @NonNull Map<@NonNull ProfileEvaluationAxis, @NonNull List<@NonNull BigDecimal>> breakpoints
  ) {
    requireNonNull(breakpoints, "axis threshold breakpoints cannot be null");
    final var breakpointMap = new EnumMap<ProfileEvaluationAxis, List<BigDecimal>>(ProfileEvaluationAxis.class);
    breakpoints.forEach((axis, values) -> breakpointMap.put(
        requireNonNull(axis, "profile evaluation axis cannot be null"),
        validateBreakpoints(values, axis)
    ));
    this.breakpoints = Map.copyOf(breakpointMap);
  }

  public static @NonNull ThresholdSegmentProfileAxisNormalizer defaultScoringCalibration() {
    return new ThresholdSegmentProfileAxisNormalizer(Map.of(
        ProfileEvaluationAxis.SPECIFIC_GRAVITY,
        BuiltInProfileAxisRanges.specificGravityScoringBreakpoints(),
        ProfileEvaluationAxis.TITRATABLE_ACIDITY,
        BuiltInProfileAxisRanges.titratableAcidityScoringBreakpoints(),
        ProfileEvaluationAxis.TANNIN,
        BuiltInProfileAxisRanges.tanninScoringBreakpoints()
    ));
  }

  @Override
  public @NonNull BigDecimal normalize(
      @NonNull ProfileEvaluationAxis axis,
      @NonNull BigDecimal value
  ) {
    requireNonNull(axis, "profile evaluation axis cannot be null");
    requireNonNull(value, "axis value cannot be null");
    final var axisBreakpoints = breakpointsFor(axis);
    final var segmentIndex = segmentIndex(axisBreakpoints, value);
    final var segmentMin = axisBreakpoints.get(segmentIndex);
    final var segmentMax = axisBreakpoints.get(segmentIndex + 1);
    final var segmentPosition = value.subtract(segmentMin)
        .divide(segmentMax.subtract(segmentMin), MATH_CONTEXT);
    return BigDecimal.valueOf(segmentIndex)
        .add(segmentPosition, MATH_CONTEXT)
        .divide(BigDecimal.valueOf(axisBreakpoints.size() - 1L), MATH_CONTEXT);
  }

  private static int segmentIndex(
      @NonNull List<@NonNull BigDecimal> axisBreakpoints,
      @NonNull BigDecimal value
  ) {
    for (int index = 0; index < axisBreakpoints.size() - 2; index++) {
      if (isLessThan(value, axisBreakpoints.get(index + 1))) {
        return index;
      }
    }
    return axisBreakpoints.size() - 2;
  }

  private static @NonNull List<@NonNull BigDecimal> validateBreakpoints(
      @NonNull List<@NonNull BigDecimal> values,
      @NonNull ProfileEvaluationAxis axis
  ) {
    requireNonNull(values, "axis threshold breakpoints cannot be null");
    if (values.size() < 2) {
      throw new DomainRuleViolationException("axis threshold breakpoints must contain at least two values: " + axis);
    }
    for (int index = 1; index < values.size(); index++) {
      if (isAtLeast(values.get(index - 1), values.get(index))) {
        throw new DomainRuleViolationException("axis threshold breakpoints must be strictly increasing: " + axis);
      }
    }
    return List.copyOf(values);
  }

  private @NonNull List<@NonNull BigDecimal> breakpointsFor(@NonNull ProfileEvaluationAxis axis) {
    return Optional.ofNullable(breakpoints.get(axis))
        .orElseThrow(() -> new DomainRuleViolationException("No threshold breakpoints configured for axis: " + axis));
  }
}
