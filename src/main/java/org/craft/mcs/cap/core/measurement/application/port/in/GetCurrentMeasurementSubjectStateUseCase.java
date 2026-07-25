package org.craft.mcs.cap.core.measurement.application.port.in;

import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;


/** Provides the current material state expected by commands that append measurement evidence. */
public interface GetCurrentMeasurementSubjectStateUseCase {

  @NonNull MaterialStateId currentStateOf(@NonNull MeasurementSubjectReference subject);
}
