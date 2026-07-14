package org.craft.mcs.cap.core.account.application.usecase;

import java.util.List;
import org.craft.mcs.cap.core.account.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.application.ActiveWorkspaceRequiredException;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Selects one active workspace without deriving user intent from collection ordering. */
@Component
public final class ActiveWorkspaceSelectionPolicy {

  public @NonNull WorkspaceMembershipView select(
      @NonNull List<@NonNull WorkspaceMembershipView> activeMemberships,
      @Nullable WorkspaceId selectedWorkspaceId
  ) {
    requireNonNull(activeMemberships, "active workspace memberships cannot be null");
    if (activeMemberships.isEmpty()) {
      throw new AccountAccessDeniedException("account has no active workspace membership");
    }
    if (selectedWorkspaceId != null) {
      return activeMemberships.stream()
          .filter(membership -> membership.workspace().workspaceId().equals(selectedWorkspaceId))
          .findFirst()
          .orElseThrow(() -> new AccountAccessDeniedException(
              "current account has no active membership in the selected workspace"
          ));
    }
    if (activeMemberships.size() == 1) {
      return activeMemberships.getFirst();
    }
    throw new ActiveWorkspaceRequiredException();
  }
}
