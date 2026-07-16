package org.craft.mcs.cap.core.account.workspace.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

/** Raised when a CAP workspace has no persisted product settings. */
@SuppressWarnings("java:S110")
public final class WorkspaceSettingsNotFoundException extends ApplicationNotFoundException {

  @Serial
  private static final long serialVersionUID = 1L;

  public WorkspaceSettingsNotFoundException(@NonNull WorkspaceId workspaceId) {
    super("workspace settings not found: " + workspaceId.value());
  }
}
