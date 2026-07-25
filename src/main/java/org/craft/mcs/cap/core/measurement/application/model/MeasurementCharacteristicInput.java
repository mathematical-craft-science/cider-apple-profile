package org.craft.mcs.cap.core.measurement.application.model;

import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementDerivationMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;


/** Valid application input supplied either directly or through repeated point observations. */
public sealed interface MeasurementCharacteristicInput
    permits DirectMeasurementCharacteristicInput, RepeatedMeasurementCharacteristicInput {

  @NonNull MeasurementCharacteristic characteristic();

  static @NonNull MeasurementCharacteristicInput direct(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull CharacteristicValue<MeasuredValue> value,
      @NonNull Instant measuredAt,
      @NonNull MeasurementProvenance provenance
  ) {
    return new DirectMeasurementCharacteristicInput(
        characteristic,
        value,
        measuredAt,
        provenance
    );
  }

  static @NonNull MeasurementCharacteristicInput repeated(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull List<@NonNull MeasurementObservationInput> observations,
      @NonNull MeasurementDerivationMethod derivationMethod
  ) {
    return new RepeatedMeasurementCharacteristicInput(
        characteristic,
        observations,
        derivationMethod
    );
  }
}
