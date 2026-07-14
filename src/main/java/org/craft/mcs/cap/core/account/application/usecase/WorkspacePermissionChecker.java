package org.craft.mcs.cap.core.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.domain.model.WorkspacePermission;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Internal account application policy that evaluates one permission in the active workspace context. */
@Component
@RequiredArgsConstructor
public final class WorkspacePermissionChecker {

  private final @NonNull CurrentWorkspaceContextResolver currentWorkspaceContextResolver;

  /** Requires the current active membership to match the workspace and grant the permission. */
  public void requirePermission(
      @NonNull WorkspaceId workspaceId,
      @NonNull WorkspacePermission permission
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(permission, "workspace permission cannot be null");

    final var activeMembership = currentWorkspaceContextResolver.resolve().activeMembership();
    if (!activeMembership.workspace().workspaceId().equals(workspaceId)
        || !activeMembership.role().workspacePermissions().contains(permission)) {
      throw new AccountAccessDeniedException(
          "current actor cannot access workspace " + workspaceId.value()
      );
    }
  }
}
