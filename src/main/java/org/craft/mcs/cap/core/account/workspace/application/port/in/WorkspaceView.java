package org.craft.mcs.cap.core.account.workspace.application.port.in;

import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceStatus;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Read model of a CAP workspace that owns product data. */
public record WorkspaceView(
    @NonNull WorkspaceId workspaceId,
    @NonNull DisplayName name,
    @NonNull WorkspaceType type,
    @NonNull WorkspaceStatus status
) {

  public WorkspaceView {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(name, "workspace name cannot be null");
    requireNonNull(type, "workspace type cannot be null");
    requireNonNull(status, "workspace status cannot be null");
  }

  /** Builds a workspace read model from the workspace aggregate. */
  public static @NonNull WorkspaceView from(@NonNull Workspace workspace) {
    requireNonNull(workspace, "workspace cannot be null");
    return new WorkspaceView(
        workspace.workspaceId(),
        workspace.name(),
        workspace.type(),
        workspace.status()
    );
  }
}
