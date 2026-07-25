package org.craft.mcs.cap.core.batchtraceability.application.port.in;

import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.batchtraceability.application.model.CurrentMaterialState;
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStateSnapshot;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Reads the authoritative current pointer or one immutable historical material state. */
public interface GetCurrentMaterialStateUseCase {

  @NonNull CurrentMaterialState getCurrent(@NonNull BatchTraceSubject subject);

  @NonNull Map<@NonNull BatchTraceSubject, @NonNull CurrentMaterialState> getCurrent(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull BatchTraceSubject> subjects
  );

  @NonNull MaterialStateSnapshot getState(@NonNull MaterialStateId materialStateId);
}
