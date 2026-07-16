package org.craft.mcs.cap.core.account.invitation.application;

import java.io.Serial;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.jspecify.annotations.NonNull;

/** Raised when a workspace invitation command references an unknown invitation. */
@SuppressWarnings("java:S110")
public final class WorkspaceInvitationNotFoundException extends ApplicationNotFoundException {

  @Serial
  private static final long serialVersionUID = 1L;

  public WorkspaceInvitationNotFoundException(@NonNull WorkspaceInvitationId invitationId) {
    super("workspace invitation not found: " + invitationId.value());
  }
}
