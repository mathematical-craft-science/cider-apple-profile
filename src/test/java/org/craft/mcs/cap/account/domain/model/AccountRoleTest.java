package org.craft.mcs.cap.account.domain.model;

import org.craft.mcs.cap.core.account.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.domain.model.WorkspacePermission;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class AccountRoleTest {

  @Test
  void grantsProductionCapabilitiesOnlyToProductionRoles() {
    assertThat(AccountRole.TECHNOLOGIST.workspacePermissions())
        .contains(
            WorkspacePermission.FEEDSTOCK_READ,
            WorkspacePermission.FEEDSTOCK_WRITE,
            WorkspacePermission.PRODUCTION_READ,
            WorkspacePermission.PRODUCTION_WRITE
        );

    assertThat(AccountRole.CATALOG_CONTRIBUTOR.workspacePermissions())
        .contains(WorkspacePermission.CATALOG_CONTRIBUTE)
        .doesNotContain(
            WorkspacePermission.FEEDSTOCK_READ,
            WorkspacePermission.FEEDSTOCK_WRITE,
            WorkspacePermission.PRODUCTION_READ,
            WorkspacePermission.PRODUCTION_WRITE
        );

    assertThat(AccountRole.LAB_CONTRIBUTOR.workspacePermissions())
        .contains(WorkspacePermission.FEEDSTOCK_READ, WorkspacePermission.LAB_CONTRIBUTE)
        .doesNotContain(WorkspacePermission.FEEDSTOCK_WRITE, WorkspacePermission.PRODUCTION_WRITE);
  }

  @Test
  void grantsWorkspaceSettingsUpdateOnlyToAdministrativeRoles() {
    assertThat(AccountRole.OWNER.workspacePermissions())
        .contains(
            WorkspacePermission.WORKSPACE_SETTINGS_READ,
            WorkspacePermission.WORKSPACE_SETTINGS_UPDATE
        );

    assertThat(AccountRole.TECHNOLOGIST.workspacePermissions())
        .contains(WorkspacePermission.WORKSPACE_SETTINGS_READ)
        .doesNotContain(WorkspacePermission.WORKSPACE_SETTINGS_UPDATE);
  }
}
