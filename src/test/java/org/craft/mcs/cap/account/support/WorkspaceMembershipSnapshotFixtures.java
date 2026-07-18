package org.craft.mcs.cap.account.support;

import org.craft.mcs.cap.core.account.context.application.model.WorkspaceMembershipSnapshot;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

public final class WorkspaceMembershipSnapshotFixtures {

  private WorkspaceMembershipSnapshotFixtures() {
  }

  public static @NonNull WorkspaceMembershipSnapshot snapshot(
      @NonNull WorkspaceMembership membership,
      @NonNull Workspace workspace
  ) {
    requireNonNull(membership, "workspace membership cannot be null");
    requireNonNull(workspace, "workspace cannot be null");
    return new WorkspaceMembershipSnapshot(
        membership.membershipId(),
        workspace.workspaceId(),
        workspace.name(),
        workspace.type(),
        workspace.status(),
        membership.role(),
        membership.status()
    );
  }
}
