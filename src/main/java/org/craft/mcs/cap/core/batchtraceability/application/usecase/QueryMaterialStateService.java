package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.batchtraceability.application.BatchTraceSubjectNotFoundException;
import org.craft.mcs.cap.core.batchtraceability.application.MaterialStateNotFoundException;
import org.craft.mcs.cap.core.batchtraceability.application.model.CurrentMaterialState;
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStateSnapshot;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.GetCurrentMaterialStateUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.RequireCurrentMaterialStateUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.CurrentMaterialStateStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;


/** Provides authorized current-state and immutable state-snapshot queries. */
public class QueryMaterialStateService
    implements GetCurrentMaterialStateUseCase, RequireCurrentMaterialStateUseCase {

  private final @NonNull CurrentMaterialStateStore currentStateStore;
  private final @NonNull BatchTraceabilityAccessControl accessControl;

  public QueryMaterialStateService(
      @NonNull CurrentMaterialStateStore currentStateStore,
      @NonNull BatchTraceabilityAccessControl accessControl
  ) {
    this.currentStateStore = requireNonNull(currentStateStore, "current material state store cannot be null");
    this.accessControl = requireNonNull(accessControl, "batch traceability access control cannot be null");
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
  public @NonNull CurrentMaterialState lockCurrent(@NonNull BatchTraceSubject subject) {
    requireNonNull(subject, "material state subject cannot be null");
    return lockCurrentState(subject);
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public @NonNull CurrentMaterialState requireCurrent(
      @NonNull BatchTraceSubject subject,
      @NonNull MaterialStateId expectedStateId
  ) {
    requireNonNull(subject, "material state subject cannot be null");
    requireNonNull(expectedStateId, "expected material state id cannot be null");
    final var current = lockCurrentState(subject);
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
    final Map<BatchTraceSubject, CurrentMaterialState> resolvedStates = new LinkedHashMap<>();
    for (final var subject : subjects) {
      final var currentState = currentStates.values().stream()
          .filter(state -> sameBusinessSubject(state.subject(), subject))
          .findFirst()
          .orElseThrow(() -> new BatchTraceSubjectNotFoundException(subject));
      resolvedStates.put(subject, currentState);
    }
    return Map.copyOf(resolvedStates);
  }

  @Override
  public @NonNull MaterialStateSnapshot getState(@NonNull MaterialStateId materialStateId) {
    requireNonNull(materialStateId, "material state id cannot be null");
    final var state = currentStateStore.findState(materialStateId)
        .orElseThrow(() -> new MaterialStateNotFoundException(materialStateId));
    accessControl.requireReadAccess(state.workspaceId());
    return state;
  }

  private @NonNull CurrentMaterialState lockCurrentState(@NonNull BatchTraceSubject subject) {
    final var current = currentStateStore.lockCurrent(subject)
        .orElseThrow(() -> new BatchTraceSubjectNotFoundException(subject));
    accessControl.requireReadAccess(current.workspaceId());
    return current;
  }

  private static boolean sameBusinessSubject(
      @NonNull BatchTraceSubject first,
      @NonNull BatchTraceSubject second
  ) {
    return first.type() == second.type() && first.id().equals(second.id());
  }
}
