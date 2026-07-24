package org.craft.mcs.cap.core.measurement.application;

import java.io.Serial;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Application failure raised when a requested logical measurement set is absent. */
@SuppressWarnings("java:S110")
public final class MeasurementSetNotFoundException extends ApplicationNotFoundException {

  @Serial
  private static final long serialVersionUID = 1L;

  public MeasurementSetNotFoundException(@NonNull MeasurementSetId measurementSetId) {
    super("measurement set not found: "
        + requireNonNull(measurementSetId, "measurement set id cannot be null").value());
  }
}
