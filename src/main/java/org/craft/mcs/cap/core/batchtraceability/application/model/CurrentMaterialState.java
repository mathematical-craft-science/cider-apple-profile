package org.craft.mcs.cap.core.batchtraceability.application.model;

import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Current immutable material state selected for one stable traceability subject. */
public record CurrentMaterialState(
    @NonNull WorkspaceId workspaceId,
    @NonNull BatchTraceSubject subject,
    @NonNull MaterialStateId materialStateId,
    long version
) {

  public CurrentMaterialState {
    requireNonNull(workspaceId, "current material state workspace id cannot be null");
    requireNonNull(subject, "current material state subject cannot be null");
    requireNonNull(materialStateId, "current material state id cannot be null");
  }
}
