package org.craft.mcs.cap.core.account.application.model;

import java.util.List;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.domain.model.AccountUser;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Verified current-account workspace context used by workspace-scoped application services. */
public record ResolvedWorkspaceContext(
    @NonNull AccountUser user,
    @NonNull List<@NonNull WorkspaceMembershipView> memberships,
    @NonNull WorkspaceMembershipView activeMembership
) {

  public ResolvedWorkspaceContext {
    requireNonNull(user, "account user cannot be null");
    memberships = List.copyOf(memberships);
    requireNonNull(activeMembership, "active workspace membership cannot be null");
  }
}
