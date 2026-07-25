package org.craft.mcs.cap.core.batchtraceability.application.model;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Summary of a current material-state projection rebuilt from immutable trace facts. */
public record CurrentMaterialStatesRebuildResult(
    @NonNull WorkspaceId workspaceId,
    int rebuiltSubjectCount
) {

  public CurrentMaterialStatesRebuildResult {
    requireNonNull(workspaceId, "rebuilt material states workspace id cannot be null");
    if (rebuiltSubjectCount < 0) {
      throw new ApplicationValidationException("rebuilt material subject count cannot be negative");
    }
  }
}
