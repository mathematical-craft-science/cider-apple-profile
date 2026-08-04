package org.craft.mcs.cap.core.traceability.application.usecase;

import java.util.stream.Stream;
import org.craft.mcs.cap.core.traceability.application.port.in.AppendBatchTraceStepUseCase;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.jspecify.annotations.NonNull;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;


/**
 * Persists a completed material transition as an immutable traceability fact.
 *
 * <p>The service deliberately does not own current-state pointers or concurrency. Material
 * aggregates have already applied and persisted the transition before this boundary is called.</p>
 */
public class AppendBatchTraceStepService implements AppendBatchTraceStepUseCase {

  private final @NonNull BatchTraceabilityStore traceabilityStore;
  private final @NonNull BatchTraceabilityAccessControl accessControl;

  public AppendBatchTraceStepService(
      @NonNull BatchTraceabilityStore traceabilityStore,
      @NonNull BatchTraceabilityAccessControl accessControl
  ) {
    this.traceabilityStore = requireNonNull(
        traceabilityStore,
        "batch traceability store cannot be null"
    );
    this.accessControl = requireNonNull(
        accessControl,
        "batch traceability access control cannot be null"
    );
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void append(@NonNull BatchTraceStep traceStep) {
    requireNonNull(traceStep, "batch trace step cannot be null");
    final var subjectTypes = Stream.concat(
        traceStep.inputs().stream(),
        traceStep.outputs().stream()
    )
        .map(BatchTraceStepSubject::subject)
        .map(BatchTraceSubject::type)
        .collect(toUnmodifiableSet());
    accessControl.requireWriteAccess(traceStep.workspaceId(), subjectTypes);
    traceabilityStore.append(traceStep);
  }
}
