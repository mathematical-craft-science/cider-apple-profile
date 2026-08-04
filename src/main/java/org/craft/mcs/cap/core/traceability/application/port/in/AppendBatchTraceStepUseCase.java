package org.craft.mcs.cap.core.traceability.application.port.in;

import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.jspecify.annotations.NonNull;


/** Appends one immutable traceability fact inside the caller's material-operation transaction. */
public interface AppendBatchTraceStepUseCase {

  void append(@NonNull BatchTraceStep traceStep);
}
