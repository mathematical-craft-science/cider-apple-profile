package org.craft.mcs.cap.core.measurement.application.port.out;

import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;


/** Verifies that immutable measurement evidence is attached to a state of the supplied subject. */
public interface ObservedMaterialStateValidator {

  @NonNull MaterialStateId requireObservedState(
      @NonNull MeasurementSubjectReference subject,
      @NonNull MaterialStateId observedStateId
  );
}
