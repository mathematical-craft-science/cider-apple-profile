package org.craft.mcs.cap.account.membership.domain.model;

import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipRuleException;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipStatus;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class WorkspaceMembershipTest {

  @Test
  void retainsIdentityAndRoleWhenCurrentAccessIsRemoved() {
    final var membership = membership(AccountRole.TECHNOLOGIST);

    assertThat(membership.remove())
        .returns(membership.membershipId(), WorkspaceMembership::membershipId)
        .returns(membership.workspaceId(), WorkspaceMembership::workspaceId)
        .returns(membership.userId(), WorkspaceMembership::userId)
        .returns(AccountRole.TECHNOLOGIST, WorkspaceMembership::role)
        .returns(WorkspaceMembershipStatus.REMOVED, WorkspaceMembership::status)
        .matches(WorkspaceMembership::isRemoved)
        .doesNotMatch(WorkspaceMembership::isActive);
  }

  @Test
  void rejoinsRemovedMembershipWithInvitationAssignedRole() {
    final var removed = membership(AccountRole.VIEWER).remove();

    assertThat(removed.rejoin(AccountRole.ADMIN))
        .returns(removed.membershipId(), WorkspaceMembership::membershipId)
        .returns(AccountRole.ADMIN, WorkspaceMembership::role)
        .returns(WorkspaceMembershipStatus.ACTIVE, WorkspaceMembership::status)
        .matches(WorkspaceMembership::isActive)
        .doesNotMatch(WorkspaceMembership::isRemoved);
  }

  @Test
  void distinguishesSuspensionReactivationFromRejoiningAfterRemoval() {
    final var membership = membership(AccountRole.TECHNOLOGIST);
    final var removed = membership.remove();

    assertThat(membership.suspend().reactivate())
        .returns(WorkspaceMembershipStatus.ACTIVE, WorkspaceMembership::status)
        .returns(AccountRole.TECHNOLOGIST, WorkspaceMembership::role);
    assertThatThrownBy(removed::reactivate)
        .isInstanceOf(WorkspaceMembershipRuleException.class)
        .hasMessage("only a suspended workspace membership can be reactivated");
    assertThatThrownBy(() -> membership.rejoin(AccountRole.ADMIN))
        .isInstanceOf(WorkspaceMembershipRuleException.class)
        .hasMessage("only a removed workspace membership can rejoin");
  }

  @Test
  void preventsAdministrationAndRepeatedRemovalOfRemovedMembership() {
    final var removed = membership(AccountRole.TECHNOLOGIST).remove();

    assertThatThrownBy(removed::remove)
        .isInstanceOf(WorkspaceMembershipRuleException.class)
        .hasMessage("a removed workspace membership cannot remove");
    assertThatThrownBy(() -> removed.changeAssignableRole(AccountRole.ADMIN))
        .isInstanceOf(WorkspaceMembershipRuleException.class)
        .hasMessage("a removed workspace membership cannot change role");
    assertThatThrownBy(() -> removed.rejoin(AccountRole.OWNER))
        .isInstanceOf(WorkspaceMembershipRuleException.class)
        .hasMessage("OWNER role can be assigned only by ownership transfer");
  }

  private static WorkspaceMembership membership(AccountRole role) {
    return WorkspaceMembership.create(WorkspaceId.newId(), AccountUserId.newId(), role);
  }
}
