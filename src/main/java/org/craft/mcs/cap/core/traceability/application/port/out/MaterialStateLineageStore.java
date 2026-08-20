package org.craft.mcs.cap.core.traceability.application.port.out;

import java.util.Map;
import org.craft.mcs.cap.core.traceability.application.contract.MaterialStateLineage;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Persistence boundary for immutable material-state ancestry. */
public interface MaterialStateLineageStore {

  @NonNull Map<@NonNull BatchTraceSubject, @NonNull MaterialStateLineage> findLineages(
      @NonNull WorkspaceId workspaceId,
      @NonNull Map<@NonNull BatchTraceSubject, @NonNull MaterialStateId> currentStates
  );
}
