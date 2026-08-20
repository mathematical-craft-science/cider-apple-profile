package org.craft.mcs.cap.core.account.invitation.application.contract;

import java.time.Duration;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Application defaults used when a workspace administrator creates or resends an invitation. */
public record WorkspaceInvitationDefaults(@NonNull Duration timeToLive) {

  public WorkspaceInvitationDefaults {
    requireNonNull(timeToLive, "workspace invitation time to live cannot be null");
    if (timeToLive.isZero() || timeToLive.isNegative()) {
      throw new ApplicationValidationException("workspace invitation time to live must be positive");
    }
  }
}
