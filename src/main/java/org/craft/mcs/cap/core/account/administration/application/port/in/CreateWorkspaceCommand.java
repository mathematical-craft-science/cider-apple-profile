package org.craft.mcs.cap.core.account.administration.application.port.in;

import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Creates an additional workspace owned by the current active CAP account. */
public record CreateWorkspaceCommand(
    @NonNull DisplayName name,
    @NonNull WorkspaceType type
) {

  public CreateWorkspaceCommand {
    requireNonNull(name, "workspace name cannot be null");
    requireNonNull(type, "workspace type cannot be null");
  }
}
