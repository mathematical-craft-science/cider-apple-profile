package org.craft.mcs.cap.core.batchtraceability.application.usecase;

import java.util.EnumSet;
import java.util.Set;
import org.craft.mcs.cap.core.batchtraceability.application.model.CurrentMaterialStatesRebuildResult;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.RebuildCurrentMaterialStatesUseCase;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityAccessControl;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.CurrentMaterialStateStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;


/**
 * Rebuilds the disposable current-state pointer projection from immutable trace facts.
 *
 * <p>A repeatable-read snapshot keeps trace facts and persisted pointers from different
 * transaction generations from being combined. Pointer reconciliation uses compare-and-set
 * writes, so serializable predicate locking is unnecessary.</p>
 */
public class RebuildCurrentMaterialStatesService implements RebuildCurrentMaterialStatesUseCase {

  private static final Set<BatchTraceSubjectType> ALL_SUBJECT_TYPES =
      Set.copyOf(EnumSet.allOf(BatchTraceSubjectType.class));

  private final @NonNull BatchTraceabilityQueryStore traceabilityQueryStore;
  private final @NonNull CurrentMaterialStateStore currentStateStore;
  private final @NonNull BatchTraceabilityAccessControl accessControl;

  public RebuildCurrentMaterialStatesService(
      @NonNull BatchTraceabilityQueryStore traceabilityQueryStore,
      @NonNull CurrentMaterialStateStore currentStateStore,
      @NonNull BatchTraceabilityAccessControl accessControl
  ) {
    this.traceabilityQueryStore = requireNonNull(
        traceabilityQueryStore,
        "batch traceability query store cannot be null"
    );
    this.currentStateStore = requireNonNull(currentStateStore, "current material state store cannot be null");
    this.accessControl = requireNonNull(accessControl, "batch traceability access control cannot be null");
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public @NonNull CurrentMaterialStatesRebuildResult rebuild(@NonNull WorkspaceId workspaceId) {
    requireNonNull(workspaceId, "material state workspace id cannot be null");
    accessControl.requireWriteAccess(workspaceId, ALL_SUBJECT_TYPES);
    final var rebuiltStates = CurrentMaterialStateReconstructor.reconstruct(
        workspaceId,
        traceabilityQueryStore.findAllByWorkspace(workspaceId)
    );
    currentStateStore.replaceAll(workspaceId, rebuiltStates);
    return new CurrentMaterialStatesRebuildResult(workspaceId, rebuiltStates.size());
  }
}
