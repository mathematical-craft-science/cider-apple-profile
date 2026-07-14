package org.craft.mcs.cap.core.account.provisioning.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.membership.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.provisioning.application.model.WorkspaceProvisioningDefaults;
import org.craft.mcs.cap.core.account.provisioning.application.port.in.EnsureCurrentAccountProvisionedUseCase;
import org.craft.mcs.cap.core.account.provisioning.application.port.out.AccountProvisioningLock;
import org.craft.mcs.cap.core.account.user.application.model.AuthenticatedActor;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentActorProvider;
import org.craft.mcs.cap.core.account.user.application.port.out.UserPreferencesRepository;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.UserPreferences;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Atomically provisions a new CAP account or synchronizes an existing provider identity.
 *
 * <p>Default workspace creation belongs only to initial account onboarding. Existing accounts
 * remain valid without active memberships so personal account and recovery flows stay available.
 */
@Component
@RequiredArgsConstructor
public final class EnsureCurrentAccountProvisionedService
    implements EnsureCurrentAccountProvisionedUseCase {

  private final @NonNull CurrentActorProvider currentActorProvider;
  private final @NonNull AccountProvisioningLock accountProvisioningLock;
  private final @NonNull AccountUserRepository accountUserRepository;
  private final @NonNull WorkspaceRepository workspaceRepository;
  private final @NonNull WorkspaceMembershipRepository membershipRepository;
  private final @NonNull WorkspaceSettingsRepository workspaceSettingsRepository;
  private final @NonNull UserPreferencesRepository userPreferencesRepository;
  private final @NonNull WorkspaceProvisioningDefaults workspaceProvisioningDefaults;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public void ensureProvisioned() {
    final var actor = currentActorProvider.currentActor()
        .orElseThrow(() -> new AccountAccessDeniedException("authenticated actor is required"));

    transactionHelper.executeInTransaction(() -> {
      accountProvisioningLock.acquire(actor.identityProvider(), actor.externalSubject());
      provision(actor);
    });
  }

  private void provision(@NonNull AuthenticatedActor actor) {
    final var provisioningAccount = resolveAccount(actor);
    final var user = provisioningAccount.user();
    if (!user.isActive()) {
      throw new AccountAccessDeniedException("CAP account is disabled");
    }

    if (provisioningAccount.newlyCreated()) {
      provisionDefaultWorkspace(user, actor);
    }

    userPreferencesRepository.findByUserId(user.userId())
        .orElseGet(() -> userPreferencesRepository.save(UserPreferences.defaults(user.userId()))); // NOSONAR
  }

  private @NonNull ProvisioningAccount resolveAccount(@NonNull AuthenticatedActor actor) {
    return accountUserRepository.findByExternalIdentity(actor.identityProvider(), actor.externalSubject())
        .map(existing -> new ProvisioningAccount(synchronizeProviderEmail(existing, actor), false))
        .orElseGet(() -> createAccount(actor));
  }

  private @NonNull ProvisioningAccount createAccount(@NonNull AuthenticatedActor actor) {
    final var account = accountUserRepository.save(AccountUser.create(
        actor.identityProvider(),
        actor.externalSubject(),
        actor.email(),
        actor.displayName(),
        actor.avatarUrl()
    ));
    return new ProvisioningAccount(account, true);
  }

  private @NonNull AccountUser synchronizeProviderEmail(
      @NonNull AccountUser existing,
      @NonNull AuthenticatedActor actor
  ) {
    final var synchronizedUser = existing.synchronizeProviderEmail(actor.email());
    return synchronizedUser.equals(existing) ? existing : accountUserRepository.save(synchronizedUser);
  }

  private void provisionDefaultWorkspace(
      @NonNull AccountUser user,
      @NonNull AuthenticatedActor actor
  ) {
    final var workspace = workspaceRepository.save(Workspace.createDefault(
        workspaceProvisioningDefaults.name(),
        workspaceProvisioningDefaults.type(),
        actor.externalOrganizationSubject() == null ? null : actor.identityProvider(),
        actor.externalOrganizationSubject()
    ));
    final var settingsDefaults = workspaceProvisioningDefaults.settings();
    workspaceSettingsRepository.create(new WorkspaceSettings(
        workspace.workspaceId(),
        settingsDefaults.measurementStaleAfter(),
        settingsDefaults.defaultAcidityBasis()
    ));
    createDefaultMembership(user, workspace);
  }

  private void createDefaultMembership(
      @NonNull AccountUser user,
      @NonNull Workspace workspace
  ) {
    membershipRepository.save(WorkspaceMembership.createOwner(workspace.workspaceId(), user.userId()));
  }

  /** Distinguishes initial onboarding from a later account state without workspace membership. */
  private record ProvisioningAccount(
      @NonNull AccountUser user,
      boolean newlyCreated
  ) {

    private ProvisioningAccount {
      requireNonNull(user, "account user cannot be null");
    }
  }
}
