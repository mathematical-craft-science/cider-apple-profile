package org.craft.mcs.cap.core.batchtraceability.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.craft.mcs.cap.core.batchtraceability.application.model.CurrentMaterialState;
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStateSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Persistence boundary for the rebuildable current-state pointer and immutable state lookup. */
public interface CurrentMaterialStateStore {

  @NonNull Optional<@NonNull CurrentMaterialState> findCurrent(@NonNull BatchTraceSubject subject);

  /**
   * Returns the current pointer while exclusively locking only the identified material subject.
   *
   * <p>The lock is held until the surrounding transaction commits or rolls back. Other subjects,
   * including subjects in the same workspace, remain independent.</p>
   */
  @NonNull Optional<@NonNull CurrentMaterialState> lockCurrent(
      @NonNull BatchTraceSubject subject
  );

  @NonNull Map<@NonNull BatchTraceSubject, @NonNull CurrentMaterialState> findCurrent(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull BatchTraceSubject> subjects
  );

  @NonNull Optional<@NonNull MaterialStateSnapshot> findState(@NonNull MaterialStateId materialStateId);

  void initialize(
      @NonNull WorkspaceId workspaceId,
      @NonNull BatchTraceSubject subject,
      @NonNull MaterialStateId initialStateId
  );

  /**
   * Atomically advances one exact subject pointer when it still references the expected state.
   *
   * <p>The adapter must lock only the identified pointer row and raise a conflict when the
   * expected state is stale.</p>
   */
  void compareAndSet(
      @NonNull WorkspaceId workspaceId,
      @NonNull BatchTraceSubject subject,
      @NonNull MaterialStateId expectedStateId,
      @NonNull MaterialStateId nextStateId
  );

  /**
   * Reconciles the disposable pointer projection with states reconstructed for one workspace.
   *
   * <p>Every supplied state must belong to the requested workspace and represent a unique business
   * subject reconstructed from immutable trace facts. The adapter must preserve concurrent pointer
   * advances and should write only divergent rows.</p>
   */
  void replaceAll(
      @NonNull WorkspaceId workspaceId,
      @NonNull List<@NonNull CurrentMaterialState> currentStates
  );
}
