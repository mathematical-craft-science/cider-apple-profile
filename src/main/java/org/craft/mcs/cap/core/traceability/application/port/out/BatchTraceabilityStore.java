package org.craft.mcs.cap.core.traceability.application.port.out;

import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.jspecify.annotations.NonNull;


public interface BatchTraceabilityStore {

  @NonNull
  BatchTraceStep append(@NonNull BatchTraceStep step);
}
