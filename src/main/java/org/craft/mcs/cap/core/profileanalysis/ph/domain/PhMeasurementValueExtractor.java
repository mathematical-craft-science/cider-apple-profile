package org.craft.mcs.cap.core.profileanalysis.ph.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.RangeMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.StatisticalMeasurement;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;


/**
 * Extracts one representative pH value from the current profile evidence.
 *
 * <p>The extractor preserves the read-side convention used by other CAP analyses: exact values are
 * used directly, ranges use their midpoint, and statistical values prefer median over mean.
 */
final class PhMeasurementValueExtractor {

  private static final int MIDPOINT_SCALE = 6;

  private PhMeasurementValueExtractor() {
  }

  static @Nullable BigDecimal representativeValue(@NonNull ChemicalProfile profile) {
    requireNonNull(profile, "chemical profile cannot be null");
    return profile.ph()
        .map(PhMeasurementValueExtractor::representativeValue)
        .orElse(null); // NOSONAR
  }

  private static @NonNull BigDecimal representativeValue(@NonNull MeasuredCharacteristic<PhValue> characteristic) {
    return switch (characteristic) {
      case ExactMeasurement<PhValue>(PhValue value) -> value.asBigDecimal();
      case RangeMeasurement<PhValue>(PhValue min, PhValue max) -> min.asBigDecimal()
          .add(max.asBigDecimal())
          .divide(BigDecimal.valueOf(2), MIDPOINT_SCALE, RoundingMode.HALF_UP);
      case StatisticalMeasurement<PhValue> statistical -> representativeStatisticalValue(statistical).asBigDecimal();
    };
  }

  private static @NonNull PhValue representativeStatisticalValue(
      @NonNull StatisticalMeasurement<PhValue> measurement
  ) {
    requireNonNull(measurement, "pH statistical measurement cannot be null");
    return requireNonNullElse(measurement.median(), measurement.mean()); // NOSONAR
  }
}
