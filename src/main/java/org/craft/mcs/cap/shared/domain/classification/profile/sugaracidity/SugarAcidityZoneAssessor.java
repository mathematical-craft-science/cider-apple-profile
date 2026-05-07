package org.craft.mcs.cap.shared.domain.classification.profile.sugaracidity;

import java.math.BigDecimal;
import java.util.Optional;
import org.craft.mcs.cap.shared.domain.classification.axis.AxisValue;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.domain.classification.rule.AxisRange;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class SugarAcidityZoneAssessor {

  private final @NonNull SugarAcidityZoneThresholds thresholds;

  public SugarAcidityZoneAssessor(@NonNull SugarAcidityZoneThresholds thresholds) {
    this.thresholds = requireNonNull(thresholds, "sugar-acidity zone thresholds cannot be null");
  }

  private static boolean is(
      @NonNull SugarAcidityLevel actual,
      @NonNull SugarAcidityLevel expected
  ) {
    return actual == expected;
  }

  private static @NonNull Optional<@NonNull BigDecimal> axisValue(
      @NonNull ChemicalProfileAxes axes,
      @NonNull ChemicalAxis axis
  ) {
    return axes.valueOf(axis).map(AxisValue::value);
  }

  private static @NonNull SugarAcidityAxisPoint pointOf(@NonNull AxisValue axisValue) {
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

  private static @NonNull SugarAcidityLevel levelOf(
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

  public @NonNull Optional<@NonNull SugarAcidityZoneAssessment> assess(
      @NonNull ChemicalProfileAxes axes
  ) {
    requireNonNull(axes, "chemical profile axes cannot be null");

    final var sugarValue = axisValue(axes, ChemicalAxis.SUGAR);
    final var acidityValue = axisValue(axes, ChemicalAxis.ACIDITY);

    if (sugarValue.isEmpty() || acidityValue.isEmpty()) {
      return Optional.empty();
    }

    final var point = new SugarAcidityProfilePoint(
        axes.valueOf(ChemicalAxis.SUGAR).map(SugarAcidityZoneAssessor::pointOf).orElseThrow(),
        axes.valueOf(ChemicalAxis.ACIDITY).map(SugarAcidityZoneAssessor::pointOf).orElseThrow(),
        axes.valueOf(ChemicalAxis.TANNINITY).map(SugarAcidityZoneAssessor::pointOf).orElse(null) // NOSONAR
    );
    final var sugar = levelOf(
        sugarValue.orElseThrow(),
        thresholds.lowSugar(),
        thresholds.mediumSugar(),
        thresholds.highSugar()
    );
    final var acidity = levelOf(
        acidityValue.orElseThrow(),
        thresholds.lowAcidity(),
        thresholds.mediumAcidity(),
        thresholds.highAcidity()
    );
    final var tannin = tanninLevel(axes);

    if (is(sugar, SugarAcidityLevel.HIGH) && is(acidity, SugarAcidityLevel.MEDIUM)) {
      return Optional.of(SugarAcidityZoneAssessment.classified(
          SugarAcidityZone.PERFECT_APPLES,
          sugar,
          acidity,
          tannin,
          isMedium(tannin),
          point
      ));
    }

    if (is(sugar, SugarAcidityLevel.HIGH) && is(acidity, SugarAcidityLevel.HIGH)) {
      return Optional.of(SugarAcidityZoneAssessment.classified(
          SugarAcidityZone.SUGAR_RICH_APPLES,
          sugar,
          acidity,
          tannin,
          false,
          point
      ));
    }

    if (is(acidity, SugarAcidityLevel.LOW)) {
      return Optional.of(SugarAcidityZoneAssessment.classified(
          SugarAcidityZone.LOW_ACIDITY_APPLES,
          sugar,
          acidity,
          tannin,
          false,
          point
      ));
    }

    if (is(sugar, SugarAcidityLevel.MEDIUM)
        && (is(acidity, SugarAcidityLevel.MEDIUM) || is(acidity, SugarAcidityLevel.HIGH))) {
      return Optional.of(SugarAcidityZoneAssessment.classified(
          SugarAcidityZone.MEDIUM_SUGAR_APPLES,
          sugar,
          acidity,
          tannin,
          false,
          point
      ));
    }

    if (is(sugar, SugarAcidityLevel.LOW)
        && (is(acidity, SugarAcidityLevel.MEDIUM) || is(acidity, SugarAcidityLevel.HIGH))) {
      return Optional.of(SugarAcidityZoneAssessment.classified(
          SugarAcidityZone.LOW_SUGAR_APPLES,
          sugar,
          acidity,
          tannin,
          false,
          point
      ));
    }

    return Optional.of(SugarAcidityZoneAssessment.unclassified(
        sugar,
        acidity,
        tannin,
        point
    ));
  }

  private boolean isMedium(@Nullable SugarAcidityLevel level) {
    return level == SugarAcidityLevel.MEDIUM;
  }

  private @Nullable SugarAcidityLevel tanninLevel(@NonNull ChemicalProfileAxes axes) {
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
}
