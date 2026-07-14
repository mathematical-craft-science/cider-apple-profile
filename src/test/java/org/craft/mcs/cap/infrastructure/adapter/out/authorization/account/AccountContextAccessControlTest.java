package org.craft.mcs.cap.infrastructure.adapter.out.authorization.account;

import java.util.UUID;
import org.craft.mcs.cap.core.account.application.port.in.WorkspacePermissionAuthorizer;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.FEEDSTOCK_READ;
import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.FEEDSTOCK_WRITE;
import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.PRODUCTION_READ;
import static org.craft.mcs.cap.core.account.domain.model.AccountPermission.PRODUCTION_WRITE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


class AccountContextAccessControlTest {

  @Test
  void mapsFeedstockOperationsToAccountPermissions() {
    final var workspaceId = WorkspaceId.of(UUID.randomUUID());
    final var authorizer = mock(WorkspacePermissionAuthorizer.class);
    final var accessControl = new AccountFeedstockAccessControl(authorizer);

    accessControl.requireReadAccess(workspaceId);
    accessControl.requireWriteAccess(workspaceId);

    verify(authorizer).requirePermission(workspaceId, FEEDSTOCK_READ);
    verify(authorizer).requirePermission(workspaceId, FEEDSTOCK_WRITE);
  }

  @Test
  void mapsProductionOperationsToAccountPermissions() {
    final var workspaceId = WorkspaceId.of(UUID.randomUUID());
    final var authorizer = mock(WorkspacePermissionAuthorizer.class);
    final var accessControl = new AccountProductionAccessControl(authorizer);

    accessControl.requireReadAccess(workspaceId);
    accessControl.requireWriteAccess(workspaceId);

    verify(authorizer).requirePermission(workspaceId, PRODUCTION_READ);
    verify(authorizer).requirePermission(workspaceId, PRODUCTION_WRITE);
  }
}
