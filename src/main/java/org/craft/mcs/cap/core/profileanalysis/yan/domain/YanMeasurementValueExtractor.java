package org.craft.mcs.cap.core.profileanalysis.yan.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.RangeMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.StatisticalMeasurement;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;


/**
 * Extracts one representative YAN value from the current profile evidence.
 *
 * <p>The extractor follows CAP read-side measurement semantics: exact values are used directly,
 * ranges use their midpoint, and statistical values prefer median over mean.
 */
final class YanMeasurementValueExtractor {

  private static final int MIDPOINT_SCALE = 6;

  private YanMeasurementValueExtractor() {
  }

  static @Nullable BigDecimal representativeValue(@NonNull ChemicalProfile profile) {
    requireNonNull(profile, "chemical profile cannot be null");
    return profile.yeastAssimilableNitrogen()
        .map(YanMeasurementValueExtractor::representativeValue)
        .orElse(null); // NOSONAR
  }

  private static @NonNull BigDecimal representativeValue(
      @NonNull MeasuredCharacteristic<YeastAssimilableNitrogenValue> characteristic
  ) {
    return switch (characteristic) {
      case ExactMeasurement<YeastAssimilableNitrogenValue>(YeastAssimilableNitrogenValue value) ->
          value.asBigDecimal();
      case RangeMeasurement<YeastAssimilableNitrogenValue>(
          YeastAssimilableNitrogenValue min,
          YeastAssimilableNitrogenValue max
      ) -> min.asBigDecimal()
          .add(max.asBigDecimal())
          .divide(BigDecimal.valueOf(2), MIDPOINT_SCALE, RoundingMode.HALF_UP);
      case StatisticalMeasurement<YeastAssimilableNitrogenValue> statistical ->
          representativeStatisticalValue(statistical).asBigDecimal();
    };
  }

  private static @NonNull YeastAssimilableNitrogenValue representativeStatisticalValue(
      @NonNull StatisticalMeasurement<YeastAssimilableNitrogenValue> measurement
  ) {
    requireNonNull(measurement, "YAN statistical measurement cannot be null");
    return requireNonNullElse(measurement.median(), measurement.mean()); // NOSONAR
  }
}
