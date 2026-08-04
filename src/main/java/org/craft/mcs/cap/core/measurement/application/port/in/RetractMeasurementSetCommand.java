package org.craft.mcs.cap.core.measurement.application.port.in;

import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRetractionReason;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Command that retracts a logical set without deleting any evidence. */
public record RetractMeasurementSetCommand(
    @NonNull MeasurementSetId measurementSetId,
    long expectedVersion,
    @NonNull MeasurementRetractionReason reason
) {

  public RetractMeasurementSetCommand {
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    requireNonNull(reason, "measurement-set retraction reason cannot be null");
    if (expectedVersion < 0) {
      throw new ApplicationValidationException("expected measurement-set version cannot be negative");
    }
  }
}
