package org.craft.mcs.cap.core.measurement.application.port.out;

import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.jspecify.annotations.NonNull;


/** Validates whether the owning material context currently permits measurement evidence changes. */
public interface MeasurementSubjectChangePolicy {

  void requireChangesAllowed(@NonNull MeasurementSubjectReference subject);
}
