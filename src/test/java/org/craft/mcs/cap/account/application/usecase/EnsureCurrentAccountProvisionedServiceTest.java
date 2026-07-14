package org.craft.mcs.cap.account.application.usecase;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.account.application.model.AuthenticatedActor;
import org.craft.mcs.cap.core.account.application.model.WorkspaceProvisioningDefaults;
import org.craft.mcs.cap.core.account.application.model.WorkspaceSettingsDefaults;
import org.craft.mcs.cap.core.account.application.port.out.AccountProvisioningLock;
import org.craft.mcs.cap.core.account.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.application.port.out.CurrentActorProvider;
import org.craft.mcs.cap.core.account.application.port.out.UserPreferencesRepository;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.core.account.application.usecase.EnsureCurrentAccountProvisionedService;
import org.craft.mcs.cap.core.account.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.domain.model.UserPreferences;
import org.craft.mcs.cap.core.account.domain.model.Workspace;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceType;
import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.craft.mcs.cap.core.account.domain.model.value.EmailAddress;
import org.craft.mcs.cap.core.account.domain.model.value.ExternalOrganizationSubject;
import org.craft.mcs.cap.core.account.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.domain.model.value.HttpUrl;
import org.craft.mcs.cap.core.account.domain.model.value.IdentityProviderCode;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class EnsureCurrentAccountProvisionedServiceTest {

  @Test
  void provisionsDefaultAccountWorkspaceMembershipSettingsAndPreferences() {
    final var currentActorProvider = mock(CurrentActorProvider.class);
    final var provisioningLock = mock(AccountProvisioningLock.class);
    final var userRepository = mock(AccountUserRepository.class);
    final var workspaceRepository = mock(WorkspaceRepository.class);
    final var membershipRepository = mock(WorkspaceMembershipRepository.class);
    final var settingsRepository = mock(WorkspaceSettingsRepository.class);
    final var preferencesRepository = mock(UserPreferencesRepository.class);
    final var persistedWorkspace = new AtomicReference<Workspace>();
    final var actor = actor();

    when(currentActorProvider.currentActor()).thenReturn(Optional.of(actor));
    when(userRepository.findByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.empty());
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, AccountUser.class));
    when(workspaceRepository.save(any())).thenAnswer(invocation -> {
      final Workspace workspace = invocation.getArgument(0);
      persistedWorkspace.set(workspace);
      return workspace;
    });
    when(membershipRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0, WorkspaceMembership.class));
    when(preferencesRepository.findByUserId(any())).thenReturn(Optional.empty());
    when(preferencesRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0, UserPreferences.class));

    final var service = new EnsureCurrentAccountProvisionedService(
        currentActorProvider,
        provisioningLock,
        userRepository,
        workspaceRepository,
        membershipRepository,
        settingsRepository,
        preferencesRepository,
        workspaceProvisioningDefaults(),
        directTransactionHelper()
    );

    service.ensureProvisioned();

    final var workspace = requireNonNull(persistedWorkspace.get());
    final var membershipCaptor = ArgumentCaptor.forClass(WorkspaceMembership.class);
    verify(membershipRepository).save(membershipCaptor.capture());
    assertThat(membershipCaptor.getValue())
        .returns(workspace.workspaceId(), WorkspaceMembership::workspaceId)
        .returns(AccountRole.OWNER, WorkspaceMembership::role)
        .matches(WorkspaceMembership::isActive);
    verify(settingsRepository).create(new WorkspaceSettings(
        workspace.workspaceId(),
        Duration.ofDays(14),
        TitratableAcidityBasis.MALIC_ACID
    ));
    verify(preferencesRepository).save(any(UserPreferences.class));
    verify(provisioningLock).acquire(actor.identityProvider(), actor.externalSubject());
  }

  @Test
  void synchronizesProviderEmailWithoutOverwritingCapOwnedProfile() {
    final var currentActorProvider = mock(CurrentActorProvider.class);
    final var provisioningLock = mock(AccountProvisioningLock.class);
    final var userRepository = mock(AccountUserRepository.class);
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
    when(userRepository.findByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.of(existingUser));
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, AccountUser.class));
    when(preferencesRepository.findByUserId(existingUser.userId()))
        .thenReturn(Optional.of(UserPreferences.defaults(existingUser.userId())));

    final var service = new EnsureCurrentAccountProvisionedService(
        currentActorProvider,
        provisioningLock,
        userRepository,
        workspaceRepository,
        membershipRepository,
        settingsRepository,
        preferencesRepository,
        workspaceProvisioningDefaults(),
        directTransactionHelper()
    );

    service.ensureProvisioned();

    final var userCaptor = ArgumentCaptor.forClass(AccountUser.class);
    verify(userRepository).save(userCaptor.capture());
    assertThat(userCaptor.getValue())
        .returns(actor.email(), AccountUser::email)
        .returns(DisplayName.of("CAP profile"), AccountUser::displayName)
        .returns(HttpUrl.of("https://cap.example.com/avatar.png"), AccountUser::avatarUrl);
  }

  @Test
  void doesNotRecreateDefaultWorkspaceForExistingAccountWithoutMemberships() {
    final var currentActorProvider = mock(CurrentActorProvider.class);
    final var provisioningLock = mock(AccountProvisioningLock.class);
    final var userRepository = mock(AccountUserRepository.class);
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
    when(userRepository.findByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.of(existingUser));
    when(preferencesRepository.findByUserId(existingUser.userId()))
        .thenReturn(Optional.of(UserPreferences.defaults(existingUser.userId())));

    final var service = new EnsureCurrentAccountProvisionedService(
        currentActorProvider,
        provisioningLock,
        userRepository,
        workspaceRepository,
        membershipRepository,
        settingsRepository,
        preferencesRepository,
        workspaceProvisioningDefaults(),
        directTransactionHelper()
    );

    service.ensureProvisioned();

    verify(userRepository, never()).save(any());
    verifyNoInteractions(workspaceRepository, membershipRepository, settingsRepository);
    verify(preferencesRepository, never()).save(any());
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

  private static @NonNull TransactionHelper directTransactionHelper() {
    return new TransactionHelper() {
      @Override
      public <T> @NonNull T callInTransactionNonNull(@NonNull Supplier<T> supplier) {
        return supplier.get();
      }

      @Override
      public void executeInTransaction(@NonNull Runnable runnable) {
        runnable.run();
      }
    };
  }
}
