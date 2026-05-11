package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.EnumMap;
import java.util.Map;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZoneThresholds;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Normalizes axes piecewise by classification thresholds:
 * normalized(x) = (segmentIndex + (x - segmentMin) / (segmentMax - segmentMin)) / segmentCount.
 *
 * <p>This normalizer is useful for target-zone matching. It makes movement across domain
 * classification thresholds more explicit than raw min-max scaling, so candidates near different
 * low/medium/high bands can be compared in terms of classification semantics.
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

  public static @NonNull ThresholdSegmentProfileAxisNormalizer from(
      @NonNull SugarAcidityZoneThresholds thresholds
  ) {
    requireNonNull(thresholds, "sugar-acidity zone thresholds cannot be null");
    return new ThresholdSegmentProfileAxisNormalizer(Map.of(
        ProfileEvaluationAxis.SUGAR,
        BuiltInProfileAxisRanges.sugarThresholdBreakpoints(),
        ProfileEvaluationAxis.ACIDITY,
        BuiltInProfileAxisRanges.acidityThresholdBreakpoints(),
        ProfileEvaluationAxis.TANNIN,
        BuiltInProfileAxisRanges.tanninThresholdBreakpoints(thresholds)
    ));
  }

  @Override
  public @NonNull BigDecimal normalize(
      @NonNull ProfileEvaluationAxis axis,
      @NonNull BigDecimal value
  ) {
    requireNonNull(axis, "profile evaluation axis cannot be null");
    requireNonNull(value, "axis value cannot be null");
    final var axisBreakpoints = breakpoints.get(axis);
    if (axisBreakpoints == null) { // NOSONAR
      throw new IllegalArgumentException("No threshold breakpoints configured for axis: " + axis);
    }
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
      if (value.compareTo(axisBreakpoints.get(index + 1)) < 0) {
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
      throw new IllegalArgumentException("axis threshold breakpoints must contain at least two values: " + axis);
    }
    for (int index = 1; index < values.size(); index++) {
      if (values.get(index - 1).compareTo(values.get(index)) >= 0) {
        throw new IllegalArgumentException("axis threshold breakpoints must be strictly increasing: " + axis);
      }
    }
    return List.copyOf(values);
  }
}
