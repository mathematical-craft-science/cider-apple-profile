package org.craft.mcs.cap.account.domain.model;

import org.craft.mcs.cap.core.account.domain.model.AccountPermission;
import org.craft.mcs.cap.core.account.domain.model.AccountRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class AccountRoleTest {

  @Test
  void grantsProductionCapabilitiesOnlyToProductionRoles() {
    assertThat(AccountRole.TECHNOLOGIST.permissions())
        .contains(
            AccountPermission.FEEDSTOCK_READ,
            AccountPermission.FEEDSTOCK_WRITE,
            AccountPermission.PRODUCTION_READ,
            AccountPermission.PRODUCTION_WRITE
        );

    assertThat(AccountRole.CATALOG_CONTRIBUTOR.permissions())
        .contains(AccountPermission.CATALOG_CONTRIBUTE)
        .doesNotContain(
            AccountPermission.FEEDSTOCK_READ,
            AccountPermission.FEEDSTOCK_WRITE,
            AccountPermission.PRODUCTION_READ,
            AccountPermission.PRODUCTION_WRITE
        );

    assertThat(AccountRole.LAB_CONTRIBUTOR.permissions())
        .contains(AccountPermission.FEEDSTOCK_READ, AccountPermission.LAB_CONTRIBUTE)
        .doesNotContain(AccountPermission.FEEDSTOCK_WRITE, AccountPermission.PRODUCTION_WRITE);
  }
}
