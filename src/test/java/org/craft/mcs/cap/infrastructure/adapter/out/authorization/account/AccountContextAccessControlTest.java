package org.craft.mcs.cap.infrastructure.adapter.out.authorization.account;

import java.util.UUID;
import org.craft.mcs.cap.core.account.application.port.in.WorkspacePermissionAuthorizer;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
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
    final var ciderMakerId = CiderMakerId.of(UUID.randomUUID());
    final var authorizer = mock(WorkspacePermissionAuthorizer.class);
    final var accessControl = new AccountFeedstockAccessControl(authorizer);

    accessControl.requireReadAccess(ciderMakerId);
    accessControl.requireWriteAccess(ciderMakerId);

    verify(authorizer).requirePermission(ciderMakerId, FEEDSTOCK_READ);
    verify(authorizer).requirePermission(ciderMakerId, FEEDSTOCK_WRITE);
  }

  @Test
  void mapsProductionOperationsToAccountPermissions() {
    final var ciderMakerId = CiderMakerId.of(UUID.randomUUID());
    final var authorizer = mock(WorkspacePermissionAuthorizer.class);
    final var accessControl = new AccountProductionAccessControl(authorizer);

    accessControl.requireReadAccess(ciderMakerId);
    accessControl.requireWriteAccess(ciderMakerId);

    verify(authorizer).requirePermission(ciderMakerId, PRODUCTION_READ);
    verify(authorizer).requirePermission(ciderMakerId, PRODUCTION_WRITE);
  }
}
