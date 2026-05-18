package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import org.craft.mcs.cap.core.batchtraceability.application.port.in.AppendBatchTraceStepUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepId;
import org.jspecify.annotations.NonNull;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;


/**
 * Application service that appends immutable batch traceability facts.
 *
 * <p>The service is deliberately thin: owning contexts validate and mutate their aggregates first,
 * then call this boundary to persist the trace step in the same transaction.
 */
public class AppendBatchTraceStepService implements AppendBatchTraceStepUseCase {

  private final @NonNull BatchTraceabilityStore store;

  public AppendBatchTraceStepService(@NonNull BatchTraceabilityStore store) {
    this.store = requireNonNull(store, "batch traceability store cannot be null");
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public @NonNull BatchTraceStepId append(@NonNull BatchTraceStep step) {
    return store.append(requireNonNull(step, "batch trace step cannot be null")).stepId();
  }
}
