package org.craft.mcs.cap.core.measurement.application.model;

import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Minimal current-revision metadata exposed to application scenarios that already hold evidence IDs.
 */
public record MeasurementSetMetadataView(
    @NonNull MeasurementSetId measurementSetId,
    @NonNull MeasurementSetName name,
    @Nullable String note,
    long version
) {

  public MeasurementSetMetadataView {
    requireNonNull(measurementSetId, "measurement set metadata id cannot be null");
    requireNonNull(name, "measurement set metadata name cannot be null");
    if (version < 0) {
      throw new ApplicationValidationException("measurement set metadata version cannot be negative");
    }
  }
}
