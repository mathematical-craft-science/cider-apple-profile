package org.craft.mcs.cap.core.traceability.application.port.in;

import java.util.Map;
import org.craft.mcs.cap.core.traceability.application.contract.MaterialStateLineage;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Public Traceability boundary for reading immutable state ancestry in bulk. */
public interface QueryMaterialStateLineageUseCase {

  @NonNull Map<@NonNull BatchTraceSubject, @NonNull MaterialStateLineage> findLineages(
      @NonNull WorkspaceId workspaceId,
      @NonNull Map<@NonNull BatchTraceSubject, @NonNull MaterialStateId> currentStates
  );
}
