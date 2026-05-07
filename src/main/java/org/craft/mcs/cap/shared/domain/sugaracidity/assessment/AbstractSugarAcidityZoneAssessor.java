package org.craft.mcs.cap.shared.domain.sugaracidity.assessment;

import java.math.BigDecimal;
import java.util.Optional;
import org.craft.mcs.cap.shared.domain.classification.axis.AxisValue;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.domain.classification.rule.AxisRange;
import org.craft.mcs.cap.shared.domain.sugaracidity.plot.SugarAcidityGraphMetadata;
import org.craft.mcs.cap.shared.domain.sugaracidity.point.SugarAcidityAxisPoint;
import org.craft.mcs.cap.shared.domain.sugaracidity.point.SugarAcidityProfilePoint;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


abstract class AbstractSugarAcidityZoneAssessor
    implements SugarAcidityZoneAssessor {

  private final @NonNull SugarAcidityZoneThresholds thresholds;
  private final @NonNull SugarAcidityGraphMetadata graphMetadata;

  protected AbstractSugarAcidityZoneAssessor(
      @NonNull SugarAcidityZoneThresholds thresholds,
      @NonNull SugarAcidityGraphMetadata graphMetadata
  ) {
    this.thresholds = requireNonNull(thresholds, "sugar-acidity zone thresholds cannot be null");
    this.graphMetadata = requireNonNull(graphMetadata, "sugar-acidity graph metadata cannot be null");
  }

  protected static boolean is(
      @NonNull SugarAcidityLevel actual,
      @NonNull SugarAcidityLevel expected
  ) {
    return actual == expected;
  }

  protected static @NonNull Optional<@NonNull BigDecimal> axisValue(
      @NonNull ChemicalProfileAxes axes,
      @NonNull ChemicalAxis axis
  ) {
    return axes.valueOf(axis).map(AxisValue::value);
  }

  protected static @NonNull SugarAcidityAxisPoint pointOf(@NonNull AxisValue axisValue) {
    requireNonNull(axisValue, "axis value cannot be null");

    return new SugarAcidityAxisPoint(
        axisValue.value(),
        axisValue.valueKind(),
        axisValue.min(),
        axisValue.max(),
        axisValue.standardDeviation(),
        axisValue.sampleSize()
    );
  }

  protected static @NonNull SugarAcidityProfilePoint profilePoint(
      @NonNull ChemicalProfileAxes axes
  ) {
    requireNonNull(axes, "chemical profile axes cannot be null");

    return new SugarAcidityProfilePoint(
        axes.valueOf(ChemicalAxis.SUGAR).map(AbstractSugarAcidityZoneAssessor::pointOf).orElseThrow(),
        axes.valueOf(ChemicalAxis.ACIDITY).map(AbstractSugarAcidityZoneAssessor::pointOf).orElseThrow(),
        axes.valueOf(ChemicalAxis.TANNINITY).map(AbstractSugarAcidityZoneAssessor::pointOf).orElse(null) // NOSONAR
    );
  }

  protected static @NonNull SugarAcidityLevel levelOf(
      @NonNull BigDecimal value,
      @NonNull AxisRange low,
      @NonNull AxisRange medium,
      @NonNull AxisRange high
  ) {
    requireNonNull(value, "axis value cannot be null");
    requireNonNull(low, "low range cannot be null");
    requireNonNull(medium, "medium range cannot be null");
    requireNonNull(high, "high range cannot be null");

    if (low.contains(value)) {
      return SugarAcidityLevel.LOW;
    }
    if (medium.contains(value)) {
      return SugarAcidityLevel.MEDIUM;
    }
    if (high.contains(value)) {
      return SugarAcidityLevel.HIGH;
    }

    throw new IllegalStateException("value does not fit any sugar-acidity range: " + value);
  }

  @Override
  public final @NonNull SugarAcidityGraphMetadata graphMetadata() {
    return graphMetadata;
  }

  protected final @NonNull SugarAcidityZoneThresholds thresholds() {
    return thresholds;
  }

  protected final @Nullable SugarAcidityLevel tanninLevel(@NonNull ChemicalProfileAxes axes) {
    final var value = axisValue(axes, ChemicalAxis.TANNINITY);

    if (value.isEmpty()) {
      return null;
    }

    if (thresholds.mediumTannin().contains(value.orElseThrow())) {
      return SugarAcidityLevel.MEDIUM;
    }

    return value.orElseThrow().compareTo(thresholds.mediumTannin().lowerBoundary()) < 0
           ? SugarAcidityLevel.LOW
           : SugarAcidityLevel.HIGH;
  }

  protected final boolean isMedium(@Nullable SugarAcidityLevel level) {
    return level == SugarAcidityLevel.MEDIUM;
  }
}
