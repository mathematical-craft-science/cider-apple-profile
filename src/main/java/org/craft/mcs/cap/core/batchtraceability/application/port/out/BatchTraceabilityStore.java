package org.craft.mcs.cap.core.batchtraceability.application.port.out;

import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.jspecify.annotations.NonNull;


public interface BatchTraceabilityStore {

  @NonNull
  BatchTraceStep append(@NonNull BatchTraceStep step);
}
