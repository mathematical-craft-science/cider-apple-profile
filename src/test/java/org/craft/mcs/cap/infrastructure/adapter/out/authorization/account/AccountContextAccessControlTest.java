package org.craft.mcs.cap.infrastructure.adapter.out.authorization.account;

import java.util.UUID;
import org.craft.mcs.cap.core.account.membership.application.port.in.WorkspacePermissionAuthorizer;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.FEEDSTOCK_READ;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.FEEDSTOCK_WRITE;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.PRODUCTION_READ;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.PRODUCTION_WRITE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


class AccountContextAccessControlTest {

  @Test
  void mapsFeedstockOperationsToWorkspacePermissions() {
    final var workspaceId = WorkspaceId.of(UUID.randomUUID());
    final var authorizer = mock(WorkspacePermissionAuthorizer.class);
    final var accessControl = new AccountFeedstockAccessControl(authorizer);

    accessControl.requireReadAccess(workspaceId);
    accessControl.requireWriteAccess(workspaceId);

    verify(authorizer).requirePermission(workspaceId, FEEDSTOCK_READ);
    verify(authorizer).requirePermission(workspaceId, FEEDSTOCK_WRITE);
  }

  @Test
  void mapsProductionOperationsToWorkspacePermissions() {
    final var workspaceId = WorkspaceId.of(UUID.randomUUID());
    final var authorizer = mock(WorkspacePermissionAuthorizer.class);
    final var accessControl = new AccountProductionAccessControl(authorizer);

    accessControl.requireReadAccess(workspaceId);
    accessControl.requireWriteAccess(workspaceId);

    verify(authorizer).requirePermission(workspaceId, PRODUCTION_READ);
    verify(authorizer).requirePermission(workspaceId, PRODUCTION_WRITE);
  }
}
