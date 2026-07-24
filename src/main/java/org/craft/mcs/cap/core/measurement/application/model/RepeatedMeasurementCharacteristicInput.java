package org.craft.mcs.cap.core.measurement.application.model;

import java.util.List;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementDerivationMethod;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Repeated POINT observations supplied with an explicit result-derivation method. */
public record RepeatedMeasurementCharacteristicInput(
    @NonNull MeasurementCharacteristic characteristic,
    @NonNull List<@NonNull MeasurementObservationInput> observations,
    @NonNull MeasurementDerivationMethod derivationMethod
) implements MeasurementCharacteristicInput {

  public RepeatedMeasurementCharacteristicInput {
    requireNonNull(characteristic, "measurement characteristic cannot be null");
    observations = List.copyOf(requireNonNull(observations, "measurement observations cannot be null"));
    requireNonNull(derivationMethod, "measurement derivation method cannot be null");
    if (observations.size() < 2) {
      throw new ApplicationValidationException("repeated measurement requires at least two observations");
    }
  }
}
