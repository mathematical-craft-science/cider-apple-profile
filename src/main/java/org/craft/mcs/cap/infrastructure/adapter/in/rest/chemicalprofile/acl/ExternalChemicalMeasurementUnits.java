package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


final class ExternalChemicalMeasurementUnits {

  private ExternalChemicalMeasurementUnits() {
  }

  static @NonNull String normalize(
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
