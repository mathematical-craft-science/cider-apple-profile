package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import java.util.Set;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.InitializeMaterialStateUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.CurrentMaterialStateStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;


/** Records a material creation fact and initializes its current-state pointer atomically. */
public class InitializeMaterialStateService implements InitializeMaterialStateUseCase {

  private final @NonNull BatchTraceabilityStore traceabilityStore;
  private final @NonNull CurrentMaterialStateStore currentStateStore;
  private final @NonNull BatchTraceabilityAccessControl accessControl;

  public InitializeMaterialStateService(
      @NonNull BatchTraceabilityStore traceabilityStore,
      @NonNull CurrentMaterialStateStore currentStateStore,
      @NonNull BatchTraceabilityAccessControl accessControl
  ) {
    this.traceabilityStore = requireNonNull(traceabilityStore, "batch traceability store cannot be null");
    this.currentStateStore = requireNonNull(currentStateStore, "current material state store cannot be null");
    this.accessControl = requireNonNull(accessControl, "batch traceability access control cannot be null");
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void initialize(
      @NonNull BatchTraceStep creationStep,
      @NonNull BatchTraceSubject subject,
      @NonNull MaterialStateId initialStateId
  ) {
    requireNonNull(creationStep, "material creation trace step cannot be null");
    requireNonNull(subject, "material state subject cannot be null");
    requireNonNull(initialStateId, "initial material state id cannot be null");
    requireStateReference(creationStep.outputs(), subject, initialStateId);
    accessControl.requireWriteAccess(creationStep.workspaceId(), Set.of(subject.type()));

    traceabilityStore.append(creationStep);
    currentStateStore.initialize(creationStep.workspaceId(), subject, initialStateId);
  }

  private static void requireStateReference(
      @NonNull Iterable<@NonNull BatchTraceStepSubject> states,
      @NonNull BatchTraceSubject subject,
      @NonNull MaterialStateId stateId
  ) {
    for (final var state : states) {
      if (state.traceStateId().equals(stateId) && sameBusinessSubject(state.subject(), subject)) {
        return;
      }
    }
    throw new ApplicationValidationException(
        "creation output must reference the declared material subject and state"
    );
  }

  private static boolean sameBusinessSubject(
      @NonNull BatchTraceSubject first,
      @NonNull BatchTraceSubject second
  ) {
    return first.type() == second.type() && first.id().equals(second.id());
  }
}
