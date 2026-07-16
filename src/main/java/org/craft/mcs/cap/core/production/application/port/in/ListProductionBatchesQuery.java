package org.craft.mcs.cap.core.production.application.port.in;

import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ListProductionBatchesQuery(@NonNull WorkspaceId workspaceId) {

  public ListProductionBatchesQuery {
    requireNonNull(workspaceId, "workspace id cannot be null");
  }
}
