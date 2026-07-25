package org.craft.mcs.cap.core.batchtraceability.application.port.in;

import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStatesTransition;
import org.jspecify.annotations.NonNull;


/** Atomically appends one trace step and moves every material-state pointer declared by it. */
public interface TransitionMaterialStatesUseCase {

  void transition(@NonNull MaterialStatesTransition transition);
}
