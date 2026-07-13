package org.craft.mcs.cap.account.application.usecase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.core.account.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.application.model.AuthenticatedActor;
import org.craft.mcs.cap.core.account.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceView;
import org.craft.mcs.cap.core.account.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.application.port.out.CurrentActorProvider;
import org.craft.mcs.cap.core.account.application.port.out.UserPreferencesRepository;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.application.usecase.GetCurrentAccountService;
import org.craft.mcs.cap.core.account.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.domain.model.AccountUserStatus;
import org.craft.mcs.cap.core.account.domain.model.Workspace;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceMembershipStatus;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceType;
import org.craft.mcs.cap.core.account.domain.model.value.AccountUserId;
import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.craft.mcs.cap.core.account.domain.model.value.EmailAddress;
import org.craft.mcs.cap.core.account.domain.model.value.ExternalOrganizationSubject;
import org.craft.mcs.cap.core.account.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.domain.model.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceId;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class GetCurrentAccountServiceTest {

  @Test
  void returnsProvisionedAccountWithoutChangingPersistentState() {
    final var currentActorProvider = mock(CurrentActorProvider.class);
    final var userRepository = mock(AccountUserRepository.class);
    final var workspaceRepository = mock(WorkspaceRepository.class);
    final var membershipRepository = mock(WorkspaceMembershipRepository.class);
    final var preferencesRepository = mock(UserPreferencesRepository.class);
    final var actor = actor();
    final var user = activeUser(actor);
    final var workspace = workspace();
    final var membership = ownerMembership(workspace, user);

    when(currentActorProvider.currentActor()).thenReturn(Optional.of(actor));
    when(userRepository.findByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.of(user));
    when(membershipRepository.findAllByUserId(user.userId())).thenReturn(List.of(membership));
    when(workspaceRepository.findById(workspace.workspaceId())).thenReturn(Optional.of(workspace));
    when(preferencesRepository.findByUserId(any())).thenReturn(Optional.empty());

    final var service = new GetCurrentAccountService(
        currentActorProvider,
        userRepository,
        workspaceRepository,
        membershipRepository,
        preferencesRepository
    );

    final var account = service.get();

    assertThat(account.user())
        .returns(EmailAddress.of("technologist@example.com"), AccountUserView::email)
        .returns(DisplayName.of("CAP Technologist"), AccountUserView::displayName);
    assertThat(account.activeWorkspace())
        .returns(DisplayName.of("My cidery"), WorkspaceView::name)
        .returns(WorkspaceType.CIDERY, WorkspaceView::type);
    assertThat(account.memberships())
        .singleElement()
        .returns(AccountRole.OWNER, WorkspaceMembershipView::role);
    assertThat(account.permissions()).containsExactlyInAnyOrderElementsOf(AccountRole.OWNER.permissions());
    verify(userRepository, never()).save(any());
    verify(workspaceRepository, never()).save(any());
    verify(membershipRepository, never()).save(any());
    verify(preferencesRepository, never()).save(any());
  }

  @Test
  void rejectsExternallyAuthenticatedIdentityWhenCapAccountIsDisabled() {
    final var currentActorProvider = mock(CurrentActorProvider.class);
    final var userRepository = mock(AccountUserRepository.class);
    final var actor = actor();
    final var disabledUser = new AccountUser(
        AccountUserId.newId(),
        actor.identityProvider(),
        actor.externalSubject(),
        actor.email(),
        actor.displayName(),
        actor.avatarUrl(),
        AccountUserStatus.DISABLED
    );
    when(currentActorProvider.currentActor()).thenReturn(Optional.of(actor));
    when(userRepository.findByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.of(disabledUser));

    final var service = new GetCurrentAccountService(
        currentActorProvider,
        userRepository,
        mock(WorkspaceRepository.class),
        mock(WorkspaceMembershipRepository.class),
        mock(UserPreferencesRepository.class)
    );

    assertThatThrownBy(service::get)
        .isInstanceOf(AccountAccessDeniedException.class)
        .hasMessage("CAP account is disabled");
  }

  private static @NonNull AccountUser activeUser(@NonNull AuthenticatedActor actor) {
    return new AccountUser(
        AccountUserId.newId(),
        actor.identityProvider(),
        actor.externalSubject(),
        actor.email(),
        actor.displayName(),
        actor.avatarUrl(),
        AccountUserStatus.ACTIVE
    );
  }

  private static @NonNull Workspace workspace() {
    return new Workspace(
        WorkspaceId.newId(),
        CiderMakerId.of(UUID.randomUUID()),
        DisplayName.of("My cidery"),
        WorkspaceType.CIDERY,
        null,
        null
    );
  }

  private static @NonNull WorkspaceMembership ownerMembership(
      @NonNull Workspace workspace,
      @NonNull AccountUser user
  ) {
    return new WorkspaceMembership(
        WorkspaceMembershipId.newId(),
        workspace.workspaceId(),
        user.userId(),
        AccountRole.OWNER,
        WorkspaceMembershipStatus.ACTIVE
    );
  }

  private static @NonNull AuthenticatedActor actor() {
    return new AuthenticatedActor(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_123"),
        EmailAddress.of("technologist@example.com"),
        DisplayName.of("CAP Technologist"),
        null,
        ExternalOrganizationSubject.of("org_123")
    );
  }

}
