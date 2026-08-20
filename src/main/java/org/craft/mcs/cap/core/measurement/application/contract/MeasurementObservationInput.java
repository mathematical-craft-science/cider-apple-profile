package org.craft.mcs.cap.core.measurement.application.contract;

import java.time.Instant;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** One independently retained observation used by a derived measurement result. */
public record MeasurementObservationInput(
    @NonNull PointCharacteristicValue<MeasuredValue> value,
    @NonNull Instant measuredAt,
    @NonNull MeasurementProvenance provenance
) {

  public MeasurementObservationInput {
    requireNonNull(value, "measurement observation value cannot be null");
    requireNonNull(measuredAt, "measurement observation time cannot be null");
    requireNonNull(provenance, "measurement observation provenance cannot be null");
  }
}
