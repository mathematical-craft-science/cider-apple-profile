package org.craft.mcs.cap.core.account.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationUnprocessableException;


/** Raised when a multi-workspace account has no explicit active workspace selection. */
@SuppressWarnings("java:S110")
public final class ActiveWorkspaceRequiredException extends ApplicationUnprocessableException {

  @Serial
  private static final long serialVersionUID = 1L;

  public ActiveWorkspaceRequiredException() {
    super("active workspace selection is required when account has multiple active workspaces");
  }
}
