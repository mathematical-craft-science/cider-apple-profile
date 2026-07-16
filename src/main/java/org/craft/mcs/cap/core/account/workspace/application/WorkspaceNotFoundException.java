package org.craft.mcs.cap.core.account.workspace.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Raised when a requested CAP workspace does not exist. */
@SuppressWarnings("java:S110")
public final class WorkspaceNotFoundException extends ApplicationNotFoundException {

  @Serial
  private static final long serialVersionUID = 1L;

  public WorkspaceNotFoundException(@NonNull WorkspaceId workspaceId) {
    super("workspace not found: " + workspaceId.value());
  }
}
