package org.craft.mcs.cap.infrastructure.adapter.out.authorization.account;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspacePermissionAuthorizer;
import org.craft.mcs.cap.core.production.batch.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.planning.application.authorization.port.out.ProductionPlanningAccessControl;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.PRODUCTION_READ;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.PRODUCTION_WRITE;


/** Translates production-owned access operations into CAP account permissions. */
@Component
@RequiredArgsConstructor
public final class AccountProductionAccessControl
    implements ProductionAccessControl, ProductionPlanningAccessControl {

  private final @NonNull WorkspacePermissionAuthorizer workspacePermissionAuthorizer;

  @Override
  public void requireReadAccess(@NonNull WorkspaceId workspaceId) {
    workspacePermissionAuthorizer.requirePermission(workspaceId, PRODUCTION_READ);
  }

  @Override
  public void requireWriteAccess(@NonNull WorkspaceId workspaceId) {
    workspacePermissionAuthorizer.requirePermission(workspaceId, PRODUCTION_WRITE);
  }
}
