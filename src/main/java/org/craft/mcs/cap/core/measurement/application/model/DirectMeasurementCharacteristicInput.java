package org.craft.mcs.cap.core.measurement.application.model;

import java.time.Instant;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** One direct POINT or INTERVAL result supplied to a measurement use case. */
public record DirectMeasurementCharacteristicInput(
    @NonNull MeasurementCharacteristic characteristic,
    @NonNull CharacteristicValue<MeasuredValue> value,
    @NonNull Instant measuredAt,
    @NonNull MeasurementProvenance provenance
) implements MeasurementCharacteristicInput {

  public DirectMeasurementCharacteristicInput {
    requireNonNull(characteristic, "measurement characteristic cannot be null");
    requireNonNull(value, "canonical measurement value cannot be null");
    requireNonNull(measuredAt, "measurement time cannot be null");
    requireNonNull(provenance, "measurement provenance cannot be null");
  }
}
