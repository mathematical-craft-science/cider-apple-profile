package org.craft.mcs.cap.core.account.application.port.in;

import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.account.domain.model.PersonalAccountCapability;
import org.craft.mcs.cap.core.account.domain.model.WorkspacePermission;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Current CAP account context used by account and workspace clients. */
public record AccountMeView(
    @NonNull AccountUserView user,
    @NonNull WorkspaceView activeWorkspace,
    @NonNull List<@NonNull WorkspaceMembershipView> memberships,
    @NonNull UserPreferencesView preferences,
    @NonNull Set<@NonNull PersonalAccountCapability> personalCapabilities,
    @NonNull Set<@NonNull WorkspacePermission> workspacePermissions
) {

  public AccountMeView {
    requireNonNull(user, "account user cannot be null");
    requireNonNull(activeWorkspace, "active workspace cannot be null");
    memberships = List.copyOf(memberships);
    requireNonNull(preferences, "user preferences cannot be null");
    personalCapabilities = Set.copyOf(personalCapabilities);
    workspacePermissions = Set.copyOf(workspacePermissions);
  }
}
