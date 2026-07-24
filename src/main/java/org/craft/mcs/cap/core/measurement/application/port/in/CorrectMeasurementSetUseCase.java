package org.craft.mcs.cap.core.measurement.application.port.in;

import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.jspecify.annotations.NonNull;


public interface CorrectMeasurementSetUseCase {

  @NonNull MeasurementSetView replace(@NonNull ReplaceMeasurementSetCommand command);

  @NonNull MeasurementSetView patch(@NonNull PatchMeasurementSetCommand command);
}
