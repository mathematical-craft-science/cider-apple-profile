package org.craft.mcs.cap.core.account.application.port.in;

import org.craft.mcs.cap.core.account.domain.model.WorkspaceType;
import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Read model of a CAP workspace that owns product data. */
public record WorkspaceView(
    @NonNull WorkspaceId workspaceId,
    @NonNull DisplayName name,
    @NonNull WorkspaceType type
) {

  public WorkspaceView {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(name, "workspace name cannot be null");
    requireNonNull(type, "workspace type cannot be null");
  }
}
