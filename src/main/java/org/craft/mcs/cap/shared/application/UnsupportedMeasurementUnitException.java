package org.craft.mcs.cap.shared.application;

import java.io.Serial;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Reports an external measurement unit that cannot be converted to a CAP canonical unit. */
public final class UnsupportedMeasurementUnitException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  public UnsupportedMeasurementUnitException(
      @NonNull String characteristic,
      @Nullable String unit
  ) {
    super(
        "Unsupported " + characteristic + " measurement unit: "
            + (unit == null ? "<default>" : unit)
    );
  }
}
