package org.craft.mcs.cap.core.profileanalysis.ph.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
/**
 * Extracts one representative pH value from the current profile evidence.
 *
 * <p>Point values are used directly; intervals use their midpoint only as an analytical
 * representative. The midpoint is not persisted as a measured result.
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

  private static @NonNull BigDecimal representativeValue(@NonNull CharacteristicValue<PhValue> characteristic) {
    return switch (characteristic) {
      case PointCharacteristicValue<PhValue>(PhValue value) -> value.asBigDecimal();
      case IntervalCharacteristicValue<PhValue>(PhValue min, PhValue max) -> min.asBigDecimal()
          .add(max.asBigDecimal())
          .divide(BigDecimal.valueOf(2), MIDPOINT_SCALE, RoundingMode.HALF_UP);
    };
  }
}
