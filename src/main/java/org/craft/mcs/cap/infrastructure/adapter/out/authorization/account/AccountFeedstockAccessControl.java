package org.craft.mcs.cap.infrastructure.adapter.out.authorization.account;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspacePermissionAuthorizer;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.planning.application.authorization.port.out.FeedstockPlanningAccessControl;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.FEEDSTOCK_READ;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.FEEDSTOCK_WRITE;


/** Translates feedstock-owned access operations into CAP account permissions. */
@Component
@RequiredArgsConstructor
public final class AccountFeedstockAccessControl
    implements FeedstockAccessControl, FeedstockPlanningAccessControl {

  private final @NonNull WorkspacePermissionAuthorizer workspacePermissionAuthorizer;

  @Override
  public void requireReadAccess(@NonNull WorkspaceId workspaceId) {
    workspacePermissionAuthorizer.requirePermission(workspaceId, FEEDSTOCK_READ);
  }

  @Override
  public void requireWriteAccess(@NonNull WorkspaceId workspaceId) {
    workspacePermissionAuthorizer.requirePermission(workspaceId, FEEDSTOCK_WRITE);
  }
}
