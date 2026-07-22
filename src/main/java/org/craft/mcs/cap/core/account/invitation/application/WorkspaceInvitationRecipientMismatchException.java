package org.craft.mcs.cap.core.account.invitation.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationForbiddenException;

/**
 * Indicates that the authenticated CAP account is not the recipient of a workspace invitation.
 *
 * <p>The exception deliberately does not expose the invited email address. Clients can use its
 * dedicated problem type to offer account switching without leaking recipient identity.
 */
@SuppressWarnings("java:S110")
public final class WorkspaceInvitationRecipientMismatchException extends ApplicationForbiddenException {

  @Serial
  private static final long serialVersionUID = 1L;

  public WorkspaceInvitationRecipientMismatchException() {
    super("workspace invitation was issued to a different account email");
  }
}
