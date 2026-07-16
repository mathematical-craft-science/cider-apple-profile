package org.craft.mcs.cap.infrastructure.adapter.out.authorization.account;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.audit.application.port.out.WorkspaceAdministrativeAuditAccessControl;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspacePermissionAuthorizer;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.WORKSPACE_AUDIT_READ;

/**
 * Adapts CAP workspace permissions to the authorization boundary required by administrative audit queries.
 */
@Component
@RequiredArgsConstructor
public final class AccountWorkspaceAdministrativeAuditAccessControl
    implements WorkspaceAdministrativeAuditAccessControl {

  private final @NonNull WorkspacePermissionAuthorizer permissionAuthorizer;

  @Override
  public void requireReadAccess(@NonNull WorkspaceId workspaceId) {
    permissionAuthorizer.requirePermission(workspaceId, WORKSPACE_AUDIT_READ);
  }
}
