package org.craft.mcs.cap.core.measurement.application.port.in;

import org.jspecify.annotations.NonNull;


/** Retracts a complete logical measurement set while retaining its immutable evidence. */
public interface RetractMeasurementSetUseCase {

  void retract(@NonNull RetractMeasurementSetCommand command);
}
