package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation;

import java.net.URI;
import org.craft.mcs.cap.core.account.invitation.application.WorkspaceInvitationRecipientMismatchException;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.craft.mcs.cap.infrastructure.adapter.in.rest.support.RestProblem.response;

/** Maps invitation-specific application failures to stable RFC 9457 problem types. */
@RestControllerAdvice(assignableTypes = WorkspaceInvitationController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class WorkspaceInvitationExceptionHandler {

  private static final @NonNull URI RECIPIENT_MISMATCH =
      URI.create("urn:problem-type:workspace-invitation-recipient-mismatch");
  private static final int FORBIDDEN = 403;

  @ExceptionHandler(WorkspaceInvitationRecipientMismatchException.class)
  public @NonNull ResponseEntity<@NonNull ProblemDetail> handleRecipientMismatch(
      @NonNull WorkspaceInvitationRecipientMismatchException exception
  ) {
    return response(
        FORBIDDEN,
        RECIPIENT_MISMATCH,
        "Workspace invitation belongs to another account",
        exception.getMessage()
    );
  }
}
