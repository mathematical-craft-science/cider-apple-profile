package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public final class UnsupportedChemicalMeasurementUnitException
    extends IllegalArgumentException {

  public UnsupportedChemicalMeasurementUnitException(
      @NonNull String axis,
      @Nullable String unit
  ) {
    super("Unsupported " + axis + " measurement unit: " + (unit == null ? "<default>" : unit));
  }
}
