package org.craft.mcs.cap.core.account.invitation.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationConflictException;
import org.jspecify.annotations.NonNull;

/** Raised when invitation acceptance or creation conflicts with current membership state. */
@SuppressWarnings("java:S110")
public final class WorkspaceInvitationConflictException extends ApplicationConflictException {

  @Serial
  private static final long serialVersionUID = 1L;

  public WorkspaceInvitationConflictException(@NonNull String message) {
    super(message);
  }
}
