package org.craft.mcs.cap.account.context.application.usecase;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.account.shared.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.context.application.port.out.ActiveWorkspaceProvider;
import org.craft.mcs.cap.core.account.context.application.port.out.CurrentAccountWorkspaceQuery;
import org.craft.mcs.cap.core.account.context.application.port.out.CurrentWorkspaceContextRequestCache;
import org.craft.mcs.cap.core.account.context.application.usecase.ActiveWorkspaceSelectionPolicy;
import org.craft.mcs.cap.core.account.context.application.usecase.CurrentAccountMembershipReader;
import org.craft.mcs.cap.core.account.context.application.usecase.CurrentWorkspaceContextResolver;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipStatus;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.user.application.model.AuthenticatedActor;
import org.craft.mcs.cap.core.account.user.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentAccountUserRequestCache;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentActorProvider;
import org.craft.mcs.cap.core.account.user.application.port.out.UserPreferencesRepository;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.craft.mcs.cap.core.account.context.application.usecase.GetCurrentAccountService;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUserStatus;
import org.craft.mcs.cap.core.account.user.domain.model.UserPreferences;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceView;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceStatus;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.craft.mcs.cap.core.account.shared.domain.value.ExternalOrganizationSubject;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.account.support.WorkspaceMembershipSnapshotFixtures.snapshot;
import static org.craft.mcs.cap.core.account.user.domain.model.PersonalAccountCapability.forActiveAccount;
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
    final var workspaceQuery = mock(CurrentAccountWorkspaceQuery.class);
    final var preferencesRepository = mock(UserPreferencesRepository.class);
    final var activeWorkspaceProvider = mock(ActiveWorkspaceProvider.class);
    final var actor = actor();
    final var user = activeUser(actor);
    final var workspace = workspace();
    final var membership = ownerMembership(workspace, user);

    when(currentActorProvider.currentActor()).thenReturn(Optional.of(actor));
    when(userRepository.findByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.of(user));
    when(workspaceQuery.findActiveByUserId(user.userId()))
        .thenReturn(List.of(snapshot(membership, workspace)));
    when(preferencesRepository.findByUserId(any())).thenReturn(Optional.empty());
    when(activeWorkspaceProvider.selectedWorkspaceId()).thenReturn(Optional.empty());

    final var service = new GetCurrentAccountService(
        currentWorkspaceContextResolver(
            currentActorProvider,
            userRepository,
            workspaceQuery,
            activeWorkspaceProvider
        ),
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
    assertThat(account.personalCapabilities()).containsExactlyInAnyOrderElementsOf(forActiveAccount());
    assertThat(account.workspacePermissions())
        .containsExactlyInAnyOrderElementsOf(AccountRole.OWNER.workspacePermissions());
    verify(userRepository, never()).create(any());
    verify(userRepository, never()).update(any());
    verify(preferencesRepository, never()).create(any());
    verify(preferencesRepository, never()).update(any());
  }

  @Test
  void usesExplicitWorkspaceInsteadOfAlphabeticalMembershipOrder() {
    final var currentActorProvider = mock(CurrentActorProvider.class);
    final var userRepository = mock(AccountUserRepository.class);
    final var workspaceQuery = mock(CurrentAccountWorkspaceQuery.class);
    final var preferencesRepository = mock(UserPreferencesRepository.class);
    final var activeWorkspaceProvider = mock(ActiveWorkspaceProvider.class);
    final var actor = actor();
    final var user = activeUser(actor);
    final var alphabeticallyFirst = workspace("Alpha cidery");
    final var selectedWorkspace = workspace("Zulu cidery");
    final var firstMembership = membership(alphabeticallyFirst, user, AccountRole.VIEWER);
    final var selectedMembership = membership(selectedWorkspace, user, AccountRole.OWNER);

    when(currentActorProvider.currentActor()).thenReturn(Optional.of(actor));
    when(userRepository.findByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.of(user));
    when(workspaceQuery.findActiveByUserId(user.userId())).thenReturn(List.of(
        snapshot(selectedMembership, selectedWorkspace),
        snapshot(firstMembership, alphabeticallyFirst)
    ));
    when(preferencesRepository.findByUserId(user.userId()))
        .thenReturn(Optional.of(UserPreferences.defaults(user.userId())));
    when(activeWorkspaceProvider.selectedWorkspaceId())
        .thenReturn(Optional.of(selectedWorkspace.workspaceId()));

    final var service = new GetCurrentAccountService(
        currentWorkspaceContextResolver(
            currentActorProvider,
            userRepository,
            workspaceQuery,
            activeWorkspaceProvider
        ),
        preferencesRepository
    );

    final var account = service.get();

    assertThat(account.activeWorkspace())
        .returns(selectedWorkspace.workspaceId(), WorkspaceView::workspaceId)
        .returns(DisplayName.of("Zulu cidery"), WorkspaceView::name);
    assertThat(account.memberships())
        .extracting(WorkspaceMembershipView::workspace)
        .extracting(WorkspaceView::name)
        .containsExactly(DisplayName.of("Alpha cidery"), DisplayName.of("Zulu cidery"));
    assertThat(account.workspacePermissions())
        .containsExactlyInAnyOrderElementsOf(AccountRole.OWNER.workspacePermissions());
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
        currentWorkspaceContextResolver(
            currentActorProvider,
            userRepository,
            mock(CurrentAccountWorkspaceQuery.class),
            mock(ActiveWorkspaceProvider.class)
        ),
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

  private static @NonNull CurrentWorkspaceContextResolver currentWorkspaceContextResolver(
      @NonNull CurrentActorProvider currentActorProvider,
      @NonNull AccountUserRepository userRepository,
      @NonNull CurrentAccountWorkspaceQuery workspaceQuery,
      @NonNull ActiveWorkspaceProvider activeWorkspaceProvider
  ) {
    return new CurrentWorkspaceContextResolver(
        new CurrentAccountUserResolver(
            currentActorProvider,
            userRepository,
            mock(CurrentAccountUserRequestCache.class)
        ),
        new CurrentAccountMembershipReader(workspaceQuery),
        activeWorkspaceProvider,
        new ActiveWorkspaceSelectionPolicy(),
        mock(CurrentWorkspaceContextRequestCache.class)
    );
  }

  private static @NonNull Workspace workspace() {
    return workspace("My cidery");
  }

  private static @NonNull Workspace workspace(@NonNull String name) {
    return new Workspace(
        WorkspaceId.newId(),
        DisplayName.of(name),
        WorkspaceType.CIDERY,
        WorkspaceStatus.ACTIVE,
        null,
        null
    );
  }

  private static @NonNull WorkspaceMembership ownerMembership(
      @NonNull Workspace workspace,
      @NonNull AccountUser user
  ) {
    return membership(workspace, user, AccountRole.OWNER);
  }

  private static @NonNull WorkspaceMembership membership(
      @NonNull Workspace workspace,
      @NonNull AccountUser user,
      @NonNull AccountRole role
  ) {
    return new WorkspaceMembership(
        WorkspaceMembershipId.newId(),
        workspace.workspaceId(),
        user.userId(),
        role,
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
