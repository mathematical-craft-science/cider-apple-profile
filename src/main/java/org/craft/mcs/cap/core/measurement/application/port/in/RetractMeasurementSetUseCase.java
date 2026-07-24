package org.craft.mcs.cap.core.measurement.application.port.in;

import org.jspecify.annotations.NonNull;


public interface RetractMeasurementSetUseCase {

  void retract(@NonNull RetractMeasurementSetCommand command);
}
