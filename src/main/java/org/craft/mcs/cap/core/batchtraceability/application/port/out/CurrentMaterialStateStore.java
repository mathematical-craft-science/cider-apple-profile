package org.craft.mcs.cap.core.batchtraceability.application.port.out;

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
   * Finds the current pointer while locking only the identified material subject until the
   * surrounding transaction completes. Other subjects, including subjects in the same workspace,
   * remain independent.
   */
  @NonNull Optional<@NonNull CurrentMaterialState> findCurrentForConsistency(
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
}
