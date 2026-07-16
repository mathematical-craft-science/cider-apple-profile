package org.craft.mcs.cap.account.context.application.usecase;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.context.application.usecase.CurrentAccountMembershipReader;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipStatus;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.user.application.model.AuthenticatedActor;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentActorProvider;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.context.application.usecase.ListAccountWorkspacesService;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ListAccountWorkspacesServiceTest {

  @Test
  void returnsEmptyListWhenCurrentAccountLostItsLastActiveMembership() {
    final var actorProvider = mock(CurrentActorProvider.class);
    final var userRepository = mock(AccountUserRepository.class);
    final var membershipRepository = mock(WorkspaceMembershipRepository.class);
    final var workspaceRepository = mock(WorkspaceRepository.class);
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
    final var membership = WorkspaceMembership.createOwner(WorkspaceId.newId(), user.userId());
    final var suspendedMembership = new WorkspaceMembership(
        membership.membershipId(),
        membership.workspaceId(),
        membership.userId(),
        membership.role(),
        WorkspaceMembershipStatus.SUSPENDED
    );

    when(actorProvider.currentActor()).thenReturn(Optional.of(actor));
    when(userRepository.findByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.of(user));
    when(membershipRepository.findAllByUserId(user.userId()))
        .thenReturn(List.of(suspendedMembership));

    final var service = new ListAccountWorkspacesService(
        new CurrentAccountUserResolver(actorProvider, userRepository),
        new CurrentAccountMembershipReader(membershipRepository, workspaceRepository)
    );

    assertThat(service.list()).isEmpty();
    verifyNoInteractions(workspaceRepository);
  }
}
