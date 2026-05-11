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
 */
public final class ThresholdSegmentBlendProfileAxisNormalizer implements BlendProfileAxisNormalizer {

  private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

  private final @NonNull Map<@NonNull BlendProfileAxis, @NonNull List<@NonNull BigDecimal>> breakpoints;

  public ThresholdSegmentBlendProfileAxisNormalizer(
      @NonNull Map<@NonNull BlendProfileAxis, @NonNull List<@NonNull BigDecimal>> breakpoints
  ) {
    requireNonNull(breakpoints, "axis threshold breakpoints cannot be null");
    final var breakpointMap = new EnumMap<BlendProfileAxis, List<BigDecimal>>(BlendProfileAxis.class);
    breakpoints.forEach((axis, values) -> breakpointMap.put(
        requireNonNull(axis, "blend profile axis cannot be null"),
        validateBreakpoints(values, axis)
    ));
    this.breakpoints = Map.copyOf(breakpointMap);
  }

  public static @NonNull ThresholdSegmentBlendProfileAxisNormalizer from(
      @NonNull SugarAcidityZoneThresholds thresholds
  ) {
    requireNonNull(thresholds, "sugar-acidity zone thresholds cannot be null");
    return new ThresholdSegmentBlendProfileAxisNormalizer(Map.of(
        BlendProfileAxis.SUGAR,
        BuiltInBlendProfileAxisRanges.sugarThresholdBreakpoints(),
        BlendProfileAxis.ACIDITY,
        BuiltInBlendProfileAxisRanges.acidityThresholdBreakpoints(),
        BlendProfileAxis.TANNIN,
        BuiltInBlendProfileAxisRanges.tanninThresholdBreakpoints(thresholds)
    ));
  }

  @Override
  public @NonNull BigDecimal normalize(
      @NonNull BlendProfileAxis axis,
      @NonNull BigDecimal value
  ) {
    requireNonNull(axis, "blend profile axis cannot be null");
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
      @NonNull BlendProfileAxis axis
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
