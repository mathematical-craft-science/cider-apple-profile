package org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


final class ExternalMeasurementUnits {

  private ExternalMeasurementUnits() {
  }

  static @NonNull String unitKey(
      @Nullable String unit,
      @NonNull String defaultUnit
  ) {
    return (unit == null || unit.isBlank() ? defaultUnit : unit)
        .strip()
        .toLowerCase()
        .replace("litre", "liter")
        .replace("liter", "l");
  }
}
