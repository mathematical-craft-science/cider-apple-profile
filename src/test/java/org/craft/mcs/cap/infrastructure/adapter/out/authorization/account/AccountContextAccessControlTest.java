package org.craft.mcs.cap.infrastructure.adapter.out.authorization.account;

import java.util.Set;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspacePermissionAuthorizer;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.FEEDSTOCK_READ;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.FEEDSTOCK_WRITE;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.PRODUCTION_READ;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.PRODUCTION_WRITE;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.WORKSPACE_AUDIT_READ;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


class AccountContextAccessControlTest {

  @Test
  void mapsFeedstockOperationsToWorkspacePermissions() {
    final var workspaceId = WorkspaceId.newId();
    final var authorizer = mock(WorkspacePermissionAuthorizer.class);
    final var accessControl = new AccountFeedstockAccessControl(authorizer);

    accessControl.requireReadAccess(workspaceId);
    accessControl.requireWriteAccess(workspaceId);

    verify(authorizer).requirePermission(workspaceId, FEEDSTOCK_READ);
    verify(authorizer).requirePermission(workspaceId, FEEDSTOCK_WRITE);
  }

  @Test
  void mapsProductionOperationsToWorkspacePermissions() {
    final var workspaceId = WorkspaceId.newId();
    final var authorizer = mock(WorkspacePermissionAuthorizer.class);
    final var accessControl = new AccountProductionAccessControl(authorizer);

    accessControl.requireReadAccess(workspaceId);
    accessControl.requireWriteAccess(workspaceId);

    verify(authorizer).requirePermission(workspaceId, PRODUCTION_READ);
    verify(authorizer).requirePermission(workspaceId, PRODUCTION_WRITE);
  }

  @Test
  void mapsTraceabilityWritesToEveryAffectedMaterialPermission() {
    final var workspaceId = WorkspaceId.newId();
    final var authorizer = mock(WorkspacePermissionAuthorizer.class);
    final var accessControl = new AccountBatchTraceabilityAccessControl(authorizer);

    accessControl.requireWriteAccess(
        workspaceId,
        Set.of(BatchTraceSubjectType.FEEDSTOCK_BATCH, BatchTraceSubjectType.PRODUCTION_BATCH)
    );

    verify(authorizer).requirePermission(workspaceId, FEEDSTOCK_WRITE);
    verify(authorizer).requirePermission(workspaceId, PRODUCTION_WRITE);
    verifyNoMoreInteractions(authorizer);
  }

  @Test
  void mapsAdministrativeAuditReadsToWorkspacePermission() {
    final var workspaceId = WorkspaceId.newId();
    final var authorizer = mock(WorkspacePermissionAuthorizer.class);
    final var accessControl = new AccountWorkspaceAdministrativeAuditAccessControl(authorizer);

    accessControl.requireReadAccess(workspaceId);

    verify(authorizer).requirePermission(workspaceId, WORKSPACE_AUDIT_READ);
  }
}
