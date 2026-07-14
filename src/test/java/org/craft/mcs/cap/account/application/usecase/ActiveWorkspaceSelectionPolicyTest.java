package org.craft.mcs.cap.account.application.usecase;

import java.util.List;
import org.craft.mcs.cap.core.account.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.application.ActiveWorkspaceRequiredException;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceView;
import org.craft.mcs.cap.core.account.application.usecase.ActiveWorkspaceSelectionPolicy;
import org.craft.mcs.cap.core.account.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceMembershipStatus;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceType;
import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ActiveWorkspaceSelectionPolicyTest {

  private final ActiveWorkspaceSelectionPolicy policy = new ActiveWorkspaceSelectionPolicy();

  @Test
  void rejectsAccountWithoutActiveWorkspaceMembership() {
    assertThatThrownBy(() -> policy.select(List.of(), null))
        .isInstanceOf(AccountAccessDeniedException.class)
        .hasMessage("account has no active workspace membership");
  }

  @Test
  void requiresExplicitSelectionForMultipleActiveWorkspaces() {
    final var memberships = List.of(membership("First"), membership("Second"));

    assertThatThrownBy(() -> policy.select(memberships, null))
        .isInstanceOf(ActiveWorkspaceRequiredException.class)
        .hasMessage("active workspace selection is required when account has multiple active workspaces");
  }

  @Test
  void rejectsWorkspaceWithoutActiveMembership() {
    final var memberships = List.of(membership("Available"));

    assertThatThrownBy(() -> policy.select(memberships, WorkspaceId.newId()))
        .isInstanceOf(AccountAccessDeniedException.class)
        .hasMessage("current account has no active membership in the selected workspace");
  }

  private static @NonNull WorkspaceMembershipView membership(@NonNull String name) {
    return new WorkspaceMembershipView(
        WorkspaceMembershipId.newId(),
        new WorkspaceView(
            WorkspaceId.newId(),
            DisplayName.of(name),
            WorkspaceType.CIDERY
        ),
        AccountRole.OWNER,
        WorkspaceMembershipStatus.ACTIVE
    );
  }
}
