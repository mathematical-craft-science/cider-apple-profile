package org.craft.mcs.cap.infrastructure.adapter.out.delivery.account.invitation.smtp;

import java.io.Serial;
import org.jspecify.annotations.NonNull;

/** Reports an invalid or unavailable infrastructure email template. */
final class WorkspaceInvitationEmailRenderingException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  WorkspaceInvitationEmailRenderingException(@NonNull String message) {
    super(message);
  }

  WorkspaceInvitationEmailRenderingException(
      @NonNull String message,
      @NonNull Throwable cause
  ) {
    super(message, cause);
  }
}
