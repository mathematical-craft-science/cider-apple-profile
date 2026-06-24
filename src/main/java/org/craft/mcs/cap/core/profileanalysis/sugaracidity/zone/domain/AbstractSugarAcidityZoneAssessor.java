package org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain;

import java.math.BigDecimal;
import java.util.Optional;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisRange;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain.SugarAcidityAxisPoint;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain.SugarAcidityProfilePoint;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isLessThan;


/**
 * Provides common threshold, axis extraction, and point-building logic for sugar-acidity zone assessors.
 */
abstract class AbstractSugarAcidityZoneAssessor
    implements SugarAcidityZoneAssessor {

  private final @NonNull SugarAcidityZoneThresholds thresholds;
  private final @NonNull SugarAcidityZoneGeometryProfile geometryProfile;

  protected AbstractSugarAcidityZoneAssessor(
      @NonNull SugarAcidityZoneThresholds thresholds,
      @NonNull SugarAcidityZoneGeometryProfile geometryProfile
  ) {
    this.thresholds = requireNonNull(thresholds, "sugar-acidity zone thresholds cannot be null");
    this.geometryProfile = requireNonNull(geometryProfile, "sugar-acidity zone geometry profile cannot be null");
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
        axes.valueOf(ChemicalAxis.TANNINITY)
            .map(AbstractSugarAcidityZoneAssessor::pointOf)
            .orElse(null) // NOSONAR
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

  protected final @NonNull SugarAcidityZoneThresholds thresholds() {
    return thresholds;
  }

  protected final @NonNull SugarAcidityZoneGeometryProfile geometryProfile() {
    return geometryProfile;
  }

  protected final @Nullable SugarAcidityLevel tanninLevel(@NonNull ChemicalProfileAxes axes) {
    final var value = axisValue(axes, ChemicalAxis.TANNINITY);

    if (value.isEmpty()) {
      return null;
    }

    if (thresholds.mediumTannin().contains(value.orElseThrow())) {
      return SugarAcidityLevel.MEDIUM;
    }

    final var mediumTanninLowerBoundary = requireNonNull(
        thresholds.mediumTannin().lowerBoundary(),
        "medium tannin lower boundary cannot be null"
    );

    return isLessThan(value.orElseThrow(), mediumTanninLowerBoundary)
           ? SugarAcidityLevel.LOW
           : SugarAcidityLevel.HIGH;
  }

  protected final boolean isMedium(@Nullable SugarAcidityLevel level) {
    return level == SugarAcidityLevel.MEDIUM;
  }
}
