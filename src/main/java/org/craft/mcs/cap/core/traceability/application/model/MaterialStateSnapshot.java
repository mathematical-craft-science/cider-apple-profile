package org.craft.mcs.cap.core.traceability.application.model;

import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Exact immutable trace snapshot associated with one material state identifier. */
public record MaterialStateSnapshot(
    @NonNull WorkspaceId workspaceId,
    @NonNull BatchTraceStepSubject state
) {

  public MaterialStateSnapshot {
    requireNonNull(workspaceId, "material state snapshot workspace id cannot be null");
    requireNonNull(state, "material state snapshot cannot be null");
  }
}
