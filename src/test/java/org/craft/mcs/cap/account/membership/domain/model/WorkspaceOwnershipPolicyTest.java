package org.craft.mcs.cap.account.membership.domain.model;

import java.util.List;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipRuleException;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceOwnershipPolicy;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkspaceOwnershipPolicyTest {

  private final @NonNull WorkspaceOwnershipPolicy policy = new WorkspaceOwnershipPolicy();

  @Test
  void preventsDeactivationOfLastActiveOwner() {
    final var owner = WorkspaceMembership.createOwner(WorkspaceId.newId(), AccountUserId.newId());

    assertThatThrownBy(() -> policy.requireCanDeactivate(owner, List.of(owner)))
        .isInstanceOf(WorkspaceMembershipRuleException.class)
        .hasMessage("the last active workspace owner cannot be deactivated");
  }

  @Test
  void transfersOwnershipAsOnePairOfMembershipStates() {
    final var workspaceId = WorkspaceId.newId();
    final var currentOwner = WorkspaceMembership.createOwner(workspaceId, AccountUserId.newId());
    final var target = WorkspaceMembership.create(workspaceId, AccountUserId.newId(), AccountRole.ADMIN);

    assertThat(policy.transfer(currentOwner, target))
        .satisfies(transfer -> assertThat(transfer.previousOwner())
            .returns(AccountRole.ADMIN, WorkspaceMembership::role)
            .returns(true, WorkspaceMembership::isActive))
        .satisfies(transfer -> assertThat(transfer.newOwner())
            .returns(AccountRole.OWNER, WorkspaceMembership::role)
            .returns(true, WorkspaceMembership::isActive));
  }
}
