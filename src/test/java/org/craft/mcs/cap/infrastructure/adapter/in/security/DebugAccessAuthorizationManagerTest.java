package org.craft.mcs.cap.infrastructure.adapter.in.security;

import java.util.List;
import org.craft.mcs.cap.core.account.application.port.in.AccountMeView;
import org.craft.mcs.cap.core.account.application.port.in.GetCurrentAccountUseCase;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.domain.model.AccountRole;
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
    final var membership = mock(WorkspaceMembershipView.class);
    final var account = mock(AccountMeView.class);
    when(account.memberships()).thenReturn(List.of(membership));
    when(accountUseCase.get()).thenReturn(account);
    final var manager = new DebugAccessAuthorizationManager(accountUseCase);

    when(membership.role()).thenReturn(AccountRole.ADMIN);
    assertThat(manager.authorize(() -> authentication, context).isGranted()).isTrue();

    when(membership.role()).thenReturn(AccountRole.TECHNOLOGIST);
    assertThat(manager.authorize(() -> authentication, context).isGranted()).isFalse();
  }

  private static Authentication authenticated() {
    final var authentication = mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(true);
    return authentication;
  }
}
