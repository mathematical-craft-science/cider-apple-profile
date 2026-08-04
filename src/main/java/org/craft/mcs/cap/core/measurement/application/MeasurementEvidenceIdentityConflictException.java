package org.craft.mcs.cap.core.measurement.application;

import java.io.Serial;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.shared.application.ApplicationConflictException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Raised when an immutable measurement record identity is already bound to different evidence. */
@SuppressWarnings("java:S110")
public final class MeasurementEvidenceIdentityConflictException extends ApplicationConflictException {

  @Serial
  private static final long serialVersionUID = 1L;

  public MeasurementEvidenceIdentityConflictException(@NonNull MeasurementRecordId recordId) {
    super("measurement evidence identity is already bound: "
        + requireNonNull(recordId, "measurement record id cannot be null").value());
  }
}
