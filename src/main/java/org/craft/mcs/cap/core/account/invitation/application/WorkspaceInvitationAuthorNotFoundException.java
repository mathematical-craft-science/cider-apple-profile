package org.craft.mcs.cap.core.account.invitation.application;

import java.io.Serial;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Raised when the persisted author of a workspace invitation cannot be resolved for delivery. */
@SuppressWarnings("java:S110")
public final class WorkspaceInvitationAuthorNotFoundException extends ApplicationNotFoundException {

  @Serial
  private static final long serialVersionUID = 1L;

  public WorkspaceInvitationAuthorNotFoundException(@NonNull AccountUserId authorUserId) {
    super(
        "workspace invitation author not found: "
            + requireNonNull(authorUserId, "invitation author user id cannot be null").value()
    );
  }
}
