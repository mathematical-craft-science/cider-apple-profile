package org.craft.mcs.cap.core.measurement.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Full replacement command that appends an immutable measurement-set revision. */
public record ReplaceMeasurementSetCommand(
    @NonNull MeasurementSetId measurementSetId,
    long expectedVersion,
    @NonNull MeasurementSetName name,
    @Nullable String note,
    @NonNull MeasurementCorrectionReason correctionReason,
    @NonNull List<@NonNull MeasurementCharacteristicInput> measurements
) {

  public ReplaceMeasurementSetCommand {
    requireNonNull(measurementSetId, "measurement set id cannot be null");
    requireNonNull(name, "measurement set name cannot be null");
    requireNonNull(correctionReason, "measurement correction reason cannot be null");
    measurements = List.copyOf(requireNonNull(measurements, "measurement inputs cannot be null"));
    if (expectedVersion < 0) {
      throw new ApplicationValidationException("expected measurement-set version cannot be negative");
    }
    if (measurements.isEmpty()) {
      throw new ApplicationValidationException("measurement replacement requires at least one characteristic");
    }
  }
}
