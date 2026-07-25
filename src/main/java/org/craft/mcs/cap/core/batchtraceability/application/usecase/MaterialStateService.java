package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.batchtraceability.application.BatchTraceSubjectNotFoundException;
import org.craft.mcs.cap.core.batchtraceability.application.MaterialStateNotFoundException;
import org.craft.mcs.cap.core.batchtraceability.application.model.CurrentMaterialState;
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStateSnapshot;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.GetCurrentMaterialStateUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.InitializeMaterialStateUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.RequireCurrentMaterialStateUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.CurrentMaterialStateStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;


/**
 * Registers initial lifecycle states and provides authorized access to material-state snapshots.
 *
 * <p>Measurements attach evidence to an existing state and therefore do not use this service to
 * advance the production lifecycle.</p>
 */
public class MaterialStateService
    implements InitializeMaterialStateUseCase,
    GetCurrentMaterialStateUseCase,
    RequireCurrentMaterialStateUseCase {

  private final @NonNull BatchTraceabilityStore traceabilityStore;
  private final @NonNull CurrentMaterialStateStore currentStateStore;
  private final @NonNull BatchTraceabilityAccessControl accessControl;

  public MaterialStateService(
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

    traceabilityStore.append(creationStep);
    currentStateStore.initialize(creationStep.workspaceId(), subject, initialStateId);
  }

  @Override
  public @NonNull CurrentMaterialState getCurrent(@NonNull BatchTraceSubject subject) {
    requireNonNull(subject, "material state subject cannot be null");
    final var current = currentStateStore.findCurrent(subject)
        .orElseThrow(() -> new BatchTraceSubjectNotFoundException(subject));
    accessControl.requireReadAccess(current.workspaceId());
    return current;
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public @NonNull CurrentMaterialState requireCurrent(
      @NonNull BatchTraceSubject subject,
      @NonNull MaterialStateId expectedStateId
  ) {
    requireNonNull(subject, "material state subject cannot be null");
    requireNonNull(expectedStateId, "expected material state id cannot be null");
    final var current = currentStateStore.findCurrentForConsistency(subject)
        .orElseThrow(() -> new BatchTraceSubjectNotFoundException(subject));
    accessControl.requireReadAccess(current.workspaceId());
    if (!current.materialStateId().equals(expectedStateId)) {
      throw new ConcurrentResourceModificationException("current material state", null);
    }
    return current;
  }

  @Override
  public @NonNull Map<@NonNull BatchTraceSubject, @NonNull CurrentMaterialState> getCurrent(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull BatchTraceSubject> subjects
  ) {
    requireNonNull(workspaceId, "material state workspace id cannot be null");
    requireNonNull(subjects, "material state subjects cannot be null");
    accessControl.requireReadAccess(workspaceId);
    final var currentStates = currentStateStore.findCurrent(workspaceId, subjects);
    subjects.forEach(subject -> {
      if (!currentStates.containsKey(subject)) {
        throw new BatchTraceSubjectNotFoundException(subject);
      }
    });
    return currentStates;
  }

  @Override
  public @NonNull MaterialStateSnapshot getState(@NonNull MaterialStateId materialStateId) {
    requireNonNull(materialStateId, "material state id cannot be null");
    final var state = currentStateStore.findState(materialStateId)
        .orElseThrow(() -> new MaterialStateNotFoundException(materialStateId));
    accessControl.requireReadAccess(state.workspaceId());
    return state;
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
