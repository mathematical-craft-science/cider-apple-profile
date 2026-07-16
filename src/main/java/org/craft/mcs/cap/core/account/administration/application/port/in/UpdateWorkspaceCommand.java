package org.craft.mcs.cap.core.account.administration.application.port.in;

import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Replaces editable metadata of an active workspace. */
public record UpdateWorkspaceCommand(
    @NonNull WorkspaceId workspaceId,
    @NonNull DisplayName name,
    @NonNull WorkspaceType type
) {

  public UpdateWorkspaceCommand {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(name, "workspace name cannot be null");
    requireNonNull(type, "workspace type cannot be null");
  }
}
