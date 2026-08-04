package org.craft.mcs.cap.core.measurement.application;

import java.io.Serial;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.shared.application.ApplicationConflictException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Application conflict raised when a requested logical measurement-set identity already exists. */
@SuppressWarnings("java:S110")
public final class MeasurementSetAlreadyExistsException extends ApplicationConflictException {

  @Serial
  private static final long serialVersionUID = 1L;

  public MeasurementSetAlreadyExistsException(
      @NonNull MeasurementSetId measurementSetId,
      @Nullable Throwable cause
  ) {
    super(
        "measurement set already exists: "
            + requireNonNull(measurementSetId, "measurement set id cannot be null").value(),
        cause
    );
  }
}
