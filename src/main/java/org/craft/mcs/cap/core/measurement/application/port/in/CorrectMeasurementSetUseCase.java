package org.craft.mcs.cap.core.measurement.application.port.in;

import org.craft.mcs.cap.core.measurement.application.contract.MeasurementSetView;
import org.jspecify.annotations.NonNull;


/** Appends immutable full or partial correction revisions to an existing measurement set. */
public interface CorrectMeasurementSetUseCase {

  @NonNull MeasurementSetView replace(@NonNull ReplaceMeasurementSetCommand command);

  @NonNull MeasurementSetView patch(@NonNull PatchMeasurementSetCommand command);
}
