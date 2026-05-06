package org.craft.mcs.cap.shared.domain.classification.axis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.domain.measurement.value.ExactMeasurement;
import org.craft.mcs.cap.shared.domain.measurement.value.MeasuredCharacteristic;
import org.craft.mcs.cap.shared.domain.measurement.value.RangeMeasurement;
import org.craft.mcs.cap.shared.domain.measurement.value.StatisticalMeasurement;
import org.craft.mcs.cap.shared.domain.measurement.value.TanninityValue;
import org.craft.mcs.cap.shared.domain.measurement.value.TitratableAcidityValue;
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

    switch (characteristic) {
      case null -> {
        return null;
      }
      case ExactMeasurement<T>(T value) -> {
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
      case RangeMeasurement<T>(T min1, T max1) -> {
        final var min = extractor.apply(min1);
        final var max = extractor.apply(max1);
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
      case StatisticalMeasurement<T>(
          T mean, T median, BigDecimal standardDeviation, Integer sampleSize
      ) -> {
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
      default -> throw new IllegalArgumentException(
          "unsupported measured characteristic type: " + characteristic.getClass().getName() // NOSONAR
      );
    }

  }

  public @NonNull ChemicalProfileAxes extract(@NonNull ChemicalProfile profile) {
    requireNonNull(profile, "chemical profile cannot be null");

    return ChemicalProfileAxes.of(
        toAxisValue(
            ChemicalAxis.SUGAR,
            profile.sugar().orElse(null), // NOSONAR
            value -> value.specificGravity().asBigDecimal()
        ),
        toAxisValue(
            ChemicalAxis.ACIDITY,
            profile.titratableAcidity().orElse(null), // NOSONAR
            TitratableAcidityValue::asBigDecimal
        ),
        toAxisValue(
            ChemicalAxis.TANNINITY,
            profile.tanninity().orElse(null), // NOSONAR
            TanninityValue::asBigDecimal
        )
    );
  }
}
