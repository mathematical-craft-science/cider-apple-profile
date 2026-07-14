package org.craft.mcs.cap.core.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.port.in.WorkspacePermissionAuthorizer;
import org.craft.mcs.cap.core.account.domain.model.WorkspacePermission;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/** Authorizes workspace permissions from persisted memberships without provisioning or writes. */
@Component
@RequiredArgsConstructor
public final class WorkspacePermissionAuthorizationService implements WorkspacePermissionAuthorizer {

  private final @NonNull WorkspacePermissionChecker workspacePermissionChecker;

  @Override
  public void requirePermission(
      @NonNull WorkspaceId workspaceId,
      @NonNull WorkspacePermission permission
  ) {
    workspacePermissionChecker.requirePermission(workspaceId, permission);
  }
}
