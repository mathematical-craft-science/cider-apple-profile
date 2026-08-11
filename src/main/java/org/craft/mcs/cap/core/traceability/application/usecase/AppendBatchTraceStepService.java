package org.craft.mcs.cap.core.traceability.application.usecase;

import java.util.stream.Stream;
import org.craft.mcs.cap.core.traceability.application.MaterialStateNotFoundException;
import org.craft.mcs.cap.core.traceability.application.port.in.AppendBatchTraceStepUseCase;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.traceability.application.port.out.MaterialStateHistoryStore;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
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
  private final @NonNull MaterialStateHistoryStore materialStateHistoryStore;
  private final @NonNull BatchTraceabilityAccessControl accessControl;

  public AppendBatchTraceStepService(
      @NonNull BatchTraceabilityStore traceabilityStore,
      @NonNull MaterialStateHistoryStore materialStateHistoryStore,
      @NonNull BatchTraceabilityAccessControl accessControl
  ) {
    this.traceabilityStore = requireNonNull(
        traceabilityStore,
        "batch traceability store cannot be null"
    );
    this.materialStateHistoryStore = requireNonNull(
        materialStateHistoryStore,
        "material state history store cannot be null"
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
    traceStep.inputs().forEach(input -> requireAuthoritativeInput(traceStep, input));
    traceabilityStore.append(traceStep);
  }

  private void requireAuthoritativeInput(
      @NonNull BatchTraceStep traceStep,
      @NonNull BatchTraceStepSubject input
  ) {
    final var persisted = materialStateHistoryStore.find(input.materialStateId())
        .orElseThrow(() -> new MaterialStateNotFoundException(input.materialStateId()));
    if (!persisted.workspaceId().equals(traceStep.workspaceId())
        || !sameIdentity(persisted.state().subject(), input.subject())) {
      throw new ApplicationValidationException(
          "trace input state does not belong to the supplied workspace and material subject"
      );
    }
  }

  private static boolean sameIdentity(
      @NonNull BatchTraceSubject first,
      @NonNull BatchTraceSubject second
  ) {
    return first.type() == second.type() && first.id().equals(second.id());
  }
}
