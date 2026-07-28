package org.craft.mcs.cap.account.provisioning.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationRepository;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.provisioning.application.model.WorkspaceProvisioningDefaults;
import org.craft.mcs.cap.core.account.provisioning.application.port.out.AccountProvisioningLock;
import org.craft.mcs.cap.core.account.provisioning.application.usecase.EnsureCurrentAccountProvisionedService;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.user.application.model.AuthenticatedActor;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentAccountUserRequestCache;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentActorProvider;
import org.craft.mcs.cap.core.account.user.application.port.out.UserPreferencesRepository;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.UserPreferences;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.workspace.application.model.WorkspaceSettingsDefaults;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.craft.mcs.cap.core.account.shared.domain.value.ExternalOrganizationSubject;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.account.support.DirectTransactionHelper.INSTANCE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.craft.mcs.cap.support.VersionedAggregateFixtures.versioned;

class EnsureCurrentAccountProvisionedServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-15T10:15:30Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  @Test
  void provisionsDefaultAccountWorkspaceMembershipSettingsAndPreferences() {
    final var currentActorProvider = mock(CurrentActorProvider.class);
    final var provisioningLock = mock(AccountProvisioningLock.class);
    final var userRepository = mock(AccountUserRepository.class);
    final var accountRequestCache = mock(CurrentAccountUserRequestCache.class);
    final var workspaceRepository = mock(WorkspaceRepository.class);
    final var membershipRepository = mock(WorkspaceMembershipRepository.class);
    final var invitationRepository = mock(WorkspaceInvitationRepository.class);
    final var settingsRepository = mock(WorkspaceSettingsRepository.class);
    final var preferencesRepository = mock(UserPreferencesRepository.class);
    final var persistedWorkspace = new AtomicReference<Workspace>();
    final var actor = actor();

    when(currentActorProvider.currentActor()).thenReturn(Optional.of(actor));
    when(userRepository.findVersionedByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.empty());
    when(userRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0, AccountUser.class));
    when(invitationRepository.findAllByInvitedEmail(actor.email())).thenReturn(List.of());
    when(workspaceRepository.create(any())).thenAnswer(invocation -> {
      final Workspace workspace = invocation.getArgument(0);
      persistedWorkspace.set(workspace);
      return workspace;
    });
    when(membershipRepository.create(any()))
        .thenAnswer(invocation -> invocation.getArgument(0, WorkspaceMembership.class));
    when(preferencesRepository.create(any()))
        .thenAnswer(invocation -> invocation.getArgument(0, UserPreferences.class));

    final var service = new EnsureCurrentAccountProvisionedService(
        currentActorProvider,
        provisioningLock,
        userRepository,
        accountRequestCache,
        workspaceRepository,
        membershipRepository,
        invitationRepository,
        settingsRepository,
        preferencesRepository,
        workspaceProvisioningDefaults(),
        INSTANCE,
        CLOCK
    );

    service.ensureProvisioned();

    final var workspace = requireNonNull(persistedWorkspace.get());
    final var membershipCaptor = ArgumentCaptor.forClass(WorkspaceMembership.class);
    verify(membershipRepository).create(membershipCaptor.capture());
    assertThat(membershipCaptor.getValue())
        .returns(workspace.workspaceId(), WorkspaceMembership::workspaceId)
        .returns(AccountRole.OWNER, WorkspaceMembership::role)
        .matches(WorkspaceMembership::isActive);
    verify(settingsRepository).create(new WorkspaceSettings(
        workspace.workspaceId(),
        Duration.ofDays(14),
        TitratableAcidityBasis.MALIC_ACID
    ));
    verify(preferencesRepository).create(any(UserPreferences.class));
    verify(provisioningLock).acquire(actor.identityProvider(), actor.externalSubject());
    verify(accountRequestCache).remember(any(AccountUser.class));
  }

  @Test
  void skipsDefaultWorkspaceWhenNewAccountHasPendingWorkspaceInvitation() {
    final var currentActorProvider = mock(CurrentActorProvider.class);
    final var provisioningLock = mock(AccountProvisioningLock.class);
    final var userRepository = mock(AccountUserRepository.class);
    final var accountRequestCache = mock(CurrentAccountUserRequestCache.class);
    final var workspaceRepository = mock(WorkspaceRepository.class);
    final var membershipRepository = mock(WorkspaceMembershipRepository.class);
    final var invitationRepository = mock(WorkspaceInvitationRepository.class);
    final var settingsRepository = mock(WorkspaceSettingsRepository.class);
    final var preferencesRepository = mock(UserPreferencesRepository.class);
    final var actor = actor();
    final var invitation = WorkspaceInvitation.create(
        WorkspaceId.newId(),
        actor.email(),
        AccountRole.ADMIN,
        AccountUserId.newId(),
        NOW,
        Duration.ofDays(7)
    );

    when(currentActorProvider.currentActor()).thenReturn(Optional.of(actor));
    when(userRepository.findVersionedByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.empty());
    when(userRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0, AccountUser.class));
    when(invitationRepository.findAllByInvitedEmail(actor.email())).thenReturn(List.of(invitation));
    when(preferencesRepository.create(any()))
        .thenAnswer(invocation -> invocation.getArgument(0, UserPreferences.class));

    final var service = new EnsureCurrentAccountProvisionedService(
        currentActorProvider,
        provisioningLock,
        userRepository,
        accountRequestCache,
        workspaceRepository,
        membershipRepository,
        invitationRepository,
        settingsRepository,
        preferencesRepository,
        workspaceProvisioningDefaults(),
        INSTANCE,
        CLOCK
    );

    service.ensureProvisioned();

    verify(userRepository).create(any(AccountUser.class));
    verify(preferencesRepository).create(any(UserPreferences.class));
    verifyNoInteractions(workspaceRepository, membershipRepository, settingsRepository);
    verify(provisioningLock).acquire(actor.identityProvider(), actor.externalSubject());
    verify(accountRequestCache).remember(any(AccountUser.class));
  }

  @Test
  void synchronizesProviderEmailWithoutOverwritingCapOwnedProfile() {
    final var currentActorProvider = mock(CurrentActorProvider.class);
    final var provisioningLock = mock(AccountProvisioningLock.class);
    final var userRepository = mock(AccountUserRepository.class);
    final var accountRequestCache = mock(CurrentAccountUserRequestCache.class);
    final var workspaceRepository = mock(WorkspaceRepository.class);
    final var membershipRepository = mock(WorkspaceMembershipRepository.class);
    final var settingsRepository = mock(WorkspaceSettingsRepository.class);
    final var preferencesRepository = mock(UserPreferencesRepository.class);
    final var existingUser = AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_123"),
        EmailAddress.of("old-provider-email@example.com"),
        DisplayName.of("Provider profile"),
        HttpUrl.of("https://provider.example.com/avatar.png")
    ).updateProfile(
        DisplayName.of("CAP profile"),
        HttpUrl.of("https://cap.example.com/avatar.png")
    );
    final var actor = new AuthenticatedActor(
        existingUser.identityProvider(),
        existingUser.externalSubject(),
        EmailAddress.of("new-provider-email@example.com"),
        DisplayName.of("Changed provider profile"),
        HttpUrl.of("https://provider.example.com/changed-avatar.png"),
        null
    );
    when(currentActorProvider.currentActor()).thenReturn(Optional.of(actor));
    when(userRepository.findVersionedByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.of(versioned(existingUser)));
    when(userRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));
    final var service = new EnsureCurrentAccountProvisionedService(
        currentActorProvider,
        provisioningLock,
        userRepository,
        accountRequestCache,
        workspaceRepository,
        membershipRepository,
        mock(WorkspaceInvitationRepository.class),
        settingsRepository,
        preferencesRepository,
        workspaceProvisioningDefaults(),
        INSTANCE,
        CLOCK
    );

    service.ensureProvisioned();

    final ArgumentCaptor<VersionedAggregate<AccountUser>> userCaptor = ArgumentCaptor.captor();
    verify(userRepository).update(userCaptor.capture());
    assertThat(userCaptor.getValue().aggregate())
        .returns(actor.email(), AccountUser::email)
        .returns(DisplayName.of("CAP profile"), AccountUser::displayName)
        .returns(HttpUrl.of("https://cap.example.com/avatar.png"), AccountUser::avatarUrl);
    verify(provisioningLock, never()).acquire(any(), any());
    verifyNoInteractions(preferencesRepository);
    verify(accountRequestCache).remember(userCaptor.getValue().aggregate());
  }

  @Test
  void acceptsProviderEmailSynchronizedByConcurrentRequest() {
    final var currentActorProvider = mock(CurrentActorProvider.class);
    final var provisioningLock = mock(AccountProvisioningLock.class);
    final var userRepository = mock(AccountUserRepository.class);
    final var accountRequestCache = mock(CurrentAccountUserRequestCache.class);
    final var actor = actor();
    final var existingUser = AccountUser.create(
        actor.identityProvider(),
        actor.externalSubject(),
        EmailAddress.of("old-provider-email@example.com"),
        actor.displayName(),
        actor.avatarUrl()
    );
    final var synchronizedUser = existingUser.synchronizeProviderEmail(actor.email());
    when(currentActorProvider.currentActor()).thenReturn(Optional.of(actor));
    when(userRepository.findVersionedByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.of(versioned(existingUser)))
        .thenReturn(Optional.of(versioned(existingUser)))
        .thenReturn(Optional.of(versioned(synchronizedUser)));
    when(userRepository.update(any())).thenThrow(
        new ConcurrentResourceModificationException("account user", null)
    );

    final var service = new EnsureCurrentAccountProvisionedService(
        currentActorProvider,
        provisioningLock,
        userRepository,
        accountRequestCache,
        mock(WorkspaceRepository.class),
        mock(WorkspaceMembershipRepository.class),
        mock(WorkspaceInvitationRepository.class),
        mock(WorkspaceSettingsRepository.class),
        mock(UserPreferencesRepository.class),
        workspaceProvisioningDefaults(),
        INSTANCE,
        CLOCK
    );

    service.ensureProvisioned();

    verify(accountRequestCache).remember(synchronizedUser);
    verifyNoInteractions(provisioningLock);
  }

  @Test
  void doesNotRecreateDefaultWorkspaceForExistingAccountWithoutMemberships() {
    final var currentActorProvider = mock(CurrentActorProvider.class);
    final var provisioningLock = mock(AccountProvisioningLock.class);
    final var userRepository = mock(AccountUserRepository.class);
    final var accountRequestCache = mock(CurrentAccountUserRequestCache.class);
    final var workspaceRepository = mock(WorkspaceRepository.class);
    final var membershipRepository = mock(WorkspaceMembershipRepository.class);
    final var settingsRepository = mock(WorkspaceSettingsRepository.class);
    final var preferencesRepository = mock(UserPreferencesRepository.class);
    final var actor = actor();
    final var existingUser = AccountUser.create(
        actor.identityProvider(),
        actor.externalSubject(),
        actor.email(),
        actor.displayName(),
        actor.avatarUrl()
    );

    when(currentActorProvider.currentActor()).thenReturn(Optional.of(actor));
    when(userRepository.findVersionedByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.of(versioned(existingUser)));
    final var service = new EnsureCurrentAccountProvisionedService(
        currentActorProvider,
        provisioningLock,
        userRepository,
        accountRequestCache,
        workspaceRepository,
        membershipRepository,
        mock(WorkspaceInvitationRepository.class),
        settingsRepository,
        preferencesRepository,
        workspaceProvisioningDefaults(),
        INSTANCE,
        CLOCK
    );

    service.ensureProvisioned();

    verify(userRepository, never()).update(any());
    verifyNoInteractions(workspaceRepository, membershipRepository, settingsRepository);
    verifyNoInteractions(provisioningLock, preferencesRepository);
    verify(accountRequestCache).remember(existingUser);
  }

  @Test
  void rechecksAccountAfterLockBeforeCreatingIt() {
    final var currentActorProvider = mock(CurrentActorProvider.class);
    final var provisioningLock = mock(AccountProvisioningLock.class);
    final var userRepository = mock(AccountUserRepository.class);
    final var accountRequestCache = mock(CurrentAccountUserRequestCache.class);
    final var workspaceRepository = mock(WorkspaceRepository.class);
    final var membershipRepository = mock(WorkspaceMembershipRepository.class);
    final var settingsRepository = mock(WorkspaceSettingsRepository.class);
    final var preferencesRepository = mock(UserPreferencesRepository.class);
    final var actor = actor();
    final var concurrentlyCreatedUser = AccountUser.create(
        actor.identityProvider(),
        actor.externalSubject(),
        actor.email(),
        actor.displayName(),
        actor.avatarUrl()
    );

    when(currentActorProvider.currentActor()).thenReturn(Optional.of(actor));
    when(userRepository.findVersionedByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.empty())
        .thenReturn(Optional.of(versioned(concurrentlyCreatedUser)));

    final var service = new EnsureCurrentAccountProvisionedService(
        currentActorProvider,
        provisioningLock,
        userRepository,
        accountRequestCache,
        workspaceRepository,
        membershipRepository,
        mock(WorkspaceInvitationRepository.class),
        settingsRepository,
        preferencesRepository,
        workspaceProvisioningDefaults(),
        INSTANCE,
        CLOCK
    );

    service.ensureProvisioned();

    verify(provisioningLock).acquire(actor.identityProvider(), actor.externalSubject());
    verify(userRepository, never()).create(any());
    verifyNoInteractions(workspaceRepository, membershipRepository, settingsRepository, preferencesRepository);
    verify(accountRequestCache).remember(concurrentlyCreatedUser);
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

  private static @NonNull WorkspaceProvisioningDefaults workspaceProvisioningDefaults() {
    return new WorkspaceProvisioningDefaults(
        DisplayName.of("My cidery"),
        WorkspaceType.CIDERY,
        new WorkspaceSettingsDefaults(
            Duration.ofDays(14),
            TitratableAcidityBasis.MALIC_ACID
        )
    );
  }

}
