package org.craft.mcs.cap.core.account.membership.application;

import java.io.Serial;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.jspecify.annotations.NonNull;

/** Raised when a workspace membership command references an unknown membership. */
@SuppressWarnings("java:S110")
public final class WorkspaceMembershipNotFoundException extends ApplicationNotFoundException {

  @Serial
  private static final long serialVersionUID = 1L;

  public WorkspaceMembershipNotFoundException(@NonNull WorkspaceMembershipId membershipId) {
    super("workspace membership not found: " + membershipId.value());
  }
}
