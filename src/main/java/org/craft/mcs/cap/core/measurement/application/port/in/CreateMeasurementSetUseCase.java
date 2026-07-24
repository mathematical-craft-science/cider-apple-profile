package org.craft.mcs.cap.core.measurement.application.port.in;

import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.jspecify.annotations.NonNull;


public interface CreateMeasurementSetUseCase {

  @NonNull MeasurementSetView create(@NonNull CreateMeasurementSetCommand command);
}
