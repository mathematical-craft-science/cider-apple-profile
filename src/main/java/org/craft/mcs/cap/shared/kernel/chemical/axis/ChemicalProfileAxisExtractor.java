package org.craft.mcs.cap.shared.kernel.chemical.axis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.RangeMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.StatisticalMeasurement;
import org.jmolecules.ddd.annotation.Service;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Service
public class ChemicalProfileAxisExtractor {

  private static final int MIDPOINT_SCALE = 6;

  private static <T extends Comparable<T>> @Nullable AxisValue toAxisValue(
      @NonNull ChemicalAxis axis,
      @Nullable MeasuredCharacteristic<T> characteristic,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor
  ) {
    requireNonNull(axis, "chemical axis cannot be null");
    requireNonNull(extractor, "axis value extractor cannot be null");

    return switch (characteristic) {
      case null -> null;
      case ExactMeasurement<T>(T value) -> exactAxisValue(axis, value, extractor);
      case RangeMeasurement<T>(T min, T max) -> rangeAxisValue(axis, min, max, extractor);
      case StatisticalMeasurement<T>(
          T mean,
          T median,
          BigDecimal standardDeviation,
          Integer sampleSize
      ) -> statisticalAxisValue(axis, mean, median, standardDeviation, sampleSize, extractor);
    };
  }

  public @NonNull ChemicalProfileAxes extract(@NonNull ChemicalProfile profile) {
    requireNonNull(profile, "chemical profile cannot be null");

    return ChemicalProfileAxes.of(
        profile.sugar()
            .map(characteristic -> toAxisValue(
                ChemicalAxis.SUGAR,
                characteristic,
                value -> value.specificGravity().asBigDecimal()
            ))
            .orElse(null),
        profile.titratableAcidity()
            .map(characteristic -> toAxisValue(ChemicalAxis.ACIDITY, characteristic, TitratableAcidityValue::asBigDecimal))
            .orElse(null),
        profile.tanninity()
            .map(characteristic -> toAxisValue(ChemicalAxis.TANNINITY, characteristic, TanninityValue::asBigDecimal))
            .orElse(null),
        profile.yeastAssimilableNitrogen()
            .map(characteristic -> toAxisValue(
                ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN,
                characteristic,
                YeastAssimilableNitrogenValue::asBigDecimal
            ))
            .orElse(null)
    );
  }

  private static <T extends Comparable<T>> @NonNull AxisValue exactAxisValue(
      @NonNull ChemicalAxis axis,
      @NonNull T value,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor
  ) {
    return new AxisValue(
        axis,
        extractor.apply(value),
        MeasurementValueKind.EXACT,
        null,
        null,
        null,
        null
    );
  }

  private static <T extends Comparable<T>> @NonNull AxisValue rangeAxisValue(
      @NonNull ChemicalAxis axis,
      @NonNull T minValue,
      @NonNull T maxValue,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor
  ) {
    final var min = extractor.apply(minValue);
    final var max = extractor.apply(maxValue);
    final var midpoint = min.add(max).divide(BigDecimal.valueOf(2), MIDPOINT_SCALE, RoundingMode.HALF_UP);

    return new AxisValue(
        axis,
        midpoint,
        MeasurementValueKind.RANGE_MIDPOINT,
        min,
        max,
        null,
        null
    );
  }

  private static <T extends Comparable<T>> @NonNull AxisValue statisticalAxisValue(
      @NonNull ChemicalAxis axis,
      @NonNull T mean,
      @Nullable T median,
      @Nullable BigDecimal standardDeviation,
      @Nullable Integer sampleSize,
      @NonNull Function<@NonNull T, @NonNull BigDecimal> extractor
  ) {
    final var hasMedian = median != null;

    return new AxisValue(
        axis,
        extractor.apply(hasMedian ? median : mean),
        hasMedian ? MeasurementValueKind.STATISTICAL_MEDIAN : MeasurementValueKind.STATISTICAL_MEAN,
        null,
        null,
        standardDeviation,
        sampleSize
    );
  }

}
