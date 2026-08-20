package org.craft.mcs.cap.account.context.application.usecase;

import java.util.Optional;
import java.util.List;
import org.craft.mcs.cap.core.account.context.application.port.out.CurrentAccountWorkspaceQuery;
import org.craft.mcs.cap.core.account.context.application.usecase.CurrentAccountMembershipReader;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.user.application.contract.AuthenticatedActor;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentAccountUserRequestCache;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentActorProvider;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.context.application.usecase.ListAccountWorkspacesService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListAccountWorkspacesServiceTest {

  @Test
  void returnsEmptyListWhenCurrentAccountLostItsLastActiveMembership() {
    final var actorProvider = mock(CurrentActorProvider.class);
    final var userRepository = mock(AccountUserRepository.class);
    final var workspaceQuery = mock(CurrentAccountWorkspaceQuery.class);
    final var actor = new AuthenticatedActor(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_123"),
        EmailAddress.of("technologist@example.com"),
        DisplayName.of("CAP Technologist"),
        null,
        null
    );
    final var user = AccountUser.create(
        actor.identityProvider(),
        actor.externalSubject(),
        actor.email(),
        actor.displayName(),
        actor.avatarUrl()
    );
    when(actorProvider.currentActor()).thenReturn(Optional.of(actor));
    when(userRepository.findByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.of(user));
    when(workspaceQuery.findActiveByUserId(user.userId())).thenReturn(List.of());

    final var service = new ListAccountWorkspacesService(
        new CurrentAccountUserResolver(
            actorProvider,
            userRepository,
            mock(CurrentAccountUserRequestCache.class)
        ),
        new CurrentAccountMembershipReader(workspaceQuery)
    );

    assertThat(service.list()).isEmpty();
  }
}
