package org.craft.mcs.cap.core.account.context.application.model;

import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipStatus;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceStatus;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Immutable read-side projection joining a membership with its workspace identity and state. */
public record WorkspaceMembershipSnapshot(
    @NonNull WorkspaceMembershipId membershipId,
    @NonNull WorkspaceId workspaceId,
    @NonNull DisplayName workspaceName,
    @NonNull WorkspaceType workspaceType,
    @NonNull WorkspaceStatus workspaceStatus,
    @NonNull AccountRole role,
    @NonNull WorkspaceMembershipStatus membershipStatus
) {

  public WorkspaceMembershipSnapshot {
    requireNonNull(membershipId, "workspace membership id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(workspaceName, "workspace name cannot be null");
    requireNonNull(workspaceType, "workspace type cannot be null");
    requireNonNull(workspaceStatus, "workspace status cannot be null");
    requireNonNull(role, "workspace role cannot be null");
    requireNonNull(membershipStatus, "workspace membership status cannot be null");
  }
}
