package org.craft.mcs.cap.account.application.usecase;

import java.time.Duration;
import java.util.List;
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
import org.craft.mcs.cap.core.account.domain.model.value.IdentityProviderCode;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
    when(membershipRepository.findAllByUserId(any())).thenReturn(List.of());
    when(workspaceRepository.save(any())).thenAnswer(invocation -> {
      final Workspace workspace = invocation.getArgument(0);
      persistedWorkspace.set(workspace);
      return workspace;
    });
    when(settingsRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, WorkspaceSettings.class));
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
    verify(settingsRepository).save(new WorkspaceSettings(
        workspace.workspaceId(),
        Duration.ofDays(14),
        TitratableAcidityBasis.MALIC_ACID
    ));
    verify(preferencesRepository).save(any(UserPreferences.class));
    verify(provisioningLock).acquire(actor.identityProvider(), actor.externalSubject());
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
