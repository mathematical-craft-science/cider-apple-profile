package org.craft.mcs.cap.infrastructure.adapter.out.production.measurement;

import java.io.Serial;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Signals an unexpected non-profile characteristic at the Production profile ACL boundary. */
final class UnsupportedProductionMeasurementCharacteristicException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  UnsupportedProductionMeasurementCharacteristicException(
      @NonNull MeasurementCharacteristic characteristic
  ) {
    super("unsupported production profile measurement characteristic: "
        + requireNonNull(characteristic, "measurement characteristic cannot be null"));
  }
}
