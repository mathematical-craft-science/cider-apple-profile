package org.craft.mcs.cap.core.batchtraceability.application.port.in;

import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepId;
import org.jspecify.annotations.NonNull;


public interface AppendBatchTraceStepUseCase {

  @NonNull
  BatchTraceStepId append(@NonNull BatchTraceStep step);
}
