package org.craft.mcs.cap.core.traceability.application.port.in;

import org.craft.mcs.cap.core.traceability.domain.graph.BatchTraceabilityGraph;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.jspecify.annotations.NonNull;


public interface QueryBatchTraceabilityUseCase {

  @NonNull
  BatchTraceabilityGraph graphFor(@NonNull BatchTraceSubject subject);

  @NonNull
  BatchTraceabilityGraph sourceGraphFor(@NonNull BatchTraceSubject subject);
}
