package org.craft.mcs.cap.infrastructure.adapter.in.security;

import java.util.List;
import org.craft.mcs.cap.core.account.application.ActiveWorkspaceRequiredException;
import org.craft.mcs.cap.core.account.application.port.in.AccountMeView;
import org.craft.mcs.cap.core.account.application.port.in.GetCurrentAccountUseCase;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceView;
import org.craft.mcs.cap.core.account.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceId;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class DebugAccessAuthorizationManagerTest {

  @Test
  void grantsDebugAccessOnlyToWorkspaceOwnerOrAdministrator() {
    final var accountUseCase = mock(GetCurrentAccountUseCase.class);
    final var authentication = authenticated();
    final var context = mock(RequestAuthorizationContext.class);
    final var workspace = mock(WorkspaceView.class);
    final var workspaceId = mock(WorkspaceId.class);
    final var membership = mock(WorkspaceMembershipView.class);
    final var account = mock(AccountMeView.class);
    when(account.activeWorkspace()).thenReturn(workspace);
    when(account.memberships()).thenReturn(List.of(membership));
    when(workspace.workspaceId()).thenReturn(workspaceId);
    when(membership.workspace()).thenReturn(workspace);
    when(membership.role()).thenReturn(AccountRole.ADMIN);
    when(accountUseCase.get()).thenReturn(account);
    final var manager = new DebugAccessAuthorizationManager(accountUseCase);

    assertThat(manager.authorize(() -> authentication, context).isGranted()).isTrue();
  }

  @Test
  void deniesDebugAccessWhenAdministrativeRoleBelongsToAnotherWorkspace() {
    final var accountUseCase = mock(GetCurrentAccountUseCase.class);
    final var authentication = authenticated();
    final var context = mock(RequestAuthorizationContext.class);
    final var activeWorkspace = mock(WorkspaceView.class);
    final var otherWorkspace = mock(WorkspaceView.class);
    final var activeWorkspaceId = mock(WorkspaceId.class);
    final var otherWorkspaceId = mock(WorkspaceId.class);
    final var activeMembership = mock(WorkspaceMembershipView.class);
    final var administrativeMembership = mock(WorkspaceMembershipView.class);
    final var account = mock(AccountMeView.class);
    when(account.activeWorkspace()).thenReturn(activeWorkspace);
    when(account.memberships()).thenReturn(List.of(activeMembership, administrativeMembership));
    when(activeWorkspace.workspaceId()).thenReturn(activeWorkspaceId);
    when(otherWorkspace.workspaceId()).thenReturn(otherWorkspaceId);
    when(activeMembership.workspace()).thenReturn(activeWorkspace);
    when(activeMembership.role()).thenReturn(AccountRole.TECHNOLOGIST);
    when(administrativeMembership.workspace()).thenReturn(otherWorkspace);
    when(administrativeMembership.role()).thenReturn(AccountRole.ADMIN);
    when(accountUseCase.get()).thenReturn(account);
    final var manager = new DebugAccessAuthorizationManager(accountUseCase);

    assertThat(manager.authorize(() -> authentication, context).isGranted()).isFalse();
  }

  @Test
  void deniesDebugAccessWhenActiveWorkspaceContextCannotBeResolved() {
    final var accountUseCase = mock(GetCurrentAccountUseCase.class);
    final var authentication = authenticated();
    final var context = mock(RequestAuthorizationContext.class);
    when(accountUseCase.get()).thenThrow(new ActiveWorkspaceRequiredException());
    final var manager = new DebugAccessAuthorizationManager(accountUseCase);

    assertThat(manager.authorize(() -> authentication, context).isGranted()).isFalse();
  }

  private static Authentication authenticated() {
    final var authentication = mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(true);
    return authentication;
  }
}
