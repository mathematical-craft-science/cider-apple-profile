package org.craft.mcs.cap.core.account.provisioning.application.usecase;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.shared.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationRepository;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.provisioning.application.model.WorkspaceProvisioningDefaults;
import org.craft.mcs.cap.core.account.provisioning.application.port.in.EnsureCurrentAccountProvisionedUseCase;
import org.craft.mcs.cap.core.account.provisioning.application.port.out.AccountProvisioningLock;
import org.craft.mcs.cap.core.account.user.application.model.AuthenticatedActor;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentAccountUserRequestCache;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentActorProvider;
import org.craft.mcs.cap.core.account.user.application.port.out.UserPreferencesRepository;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.UserPreferences;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus.PENDING;

/**
 * Atomically provisions a new CAP account or synchronizes an existing provider identity.
 *
 * <p>Default workspace creation belongs to initial account onboarding only when the account has no pending workspace
 * invitations. Existing accounts remain valid without active memberships so personal account and recovery flows stay
 * available.
 */
@Component
@RequiredArgsConstructor
public final class EnsureCurrentAccountProvisionedService
    implements EnsureCurrentAccountProvisionedUseCase {

  private final @NonNull CurrentActorProvider currentActorProvider;
  private final @NonNull AccountProvisioningLock accountProvisioningLock;
  private final @NonNull AccountUserRepository accountUserRepository;
  private final @NonNull CurrentAccountUserRequestCache currentAccountUserRequestCache;
  private final @NonNull WorkspaceRepository workspaceRepository;
  private final @NonNull WorkspaceMembershipRepository membershipRepository;
  private final @NonNull WorkspaceInvitationRepository invitationRepository;
  private final @NonNull WorkspaceSettingsRepository workspaceSettingsRepository;
  private final @NonNull UserPreferencesRepository userPreferencesRepository;
  private final @NonNull WorkspaceProvisioningDefaults workspaceProvisioningDefaults;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull Clock clock;

  @Override
  public void ensureProvisioned() {
    final var actor = currentActorProvider.currentActor()
        .orElseThrow(() -> new AccountAccessDeniedException("authenticated actor is required"));

    final var existing = accountUserRepository.findVersionedByExternalIdentity(
        actor.identityProvider(),
        actor.externalSubject()
    );
    if (existing.isPresent()) {
      currentAccountUserRequestCache.remember(ensureExistingAccount(existing.orElseThrow(), actor));
      return;
    }

    final var provisioned = transactionHelper.callInTransactionNonNull(() -> {
      accountProvisioningLock.acquire(actor.identityProvider(), actor.externalSubject());
      return accountUserRepository.findVersionedByExternalIdentity(
          actor.identityProvider(),
          actor.externalSubject()
      ).map(account -> requireActiveAccount(synchronizeProviderEmail(account, actor)))
          .orElseGet(() -> provisionNewAccount(actor));
    });
    currentAccountUserRequestCache.remember(provisioned);
  }

  private @NonNull AccountUser ensureExistingAccount(
      @NonNull VersionedAggregate<AccountUser> existing,
      @NonNull AuthenticatedActor actor
  ) {
    requireActiveAccount(existing.aggregate());
    if (existing.aggregate().email().equals(actor.email())) {
      return existing.aggregate();
    }

    try {
      return synchronizeExistingAccount(actor);
    } catch (ConcurrentResourceModificationException exception) {
      final var synchronizedByAnotherRequest = accountUserRepository.findVersionedByExternalIdentity(
          actor.identityProvider(),
          actor.externalSubject()
      ).map(VersionedAggregate::aggregate)
          .map(this::requireActiveAccount)
          .filter(user -> user.email().equals(actor.email()));
      return synchronizedByAnotherRequest.orElseThrow(() -> exception);
    }
  }

  private @NonNull AccountUser synchronizeExistingAccount(@NonNull AuthenticatedActor actor) {
    return transactionHelper.callInTransactionNonNull(() -> accountUserRepository.findVersionedByExternalIdentity(
        actor.identityProvider(),
        actor.externalSubject()
    ).map(account -> synchronizeProviderEmail(account, actor))
        .map(this::requireActiveAccount)
        .orElseThrow(() -> new AccountAccessDeniedException("CAP account no longer exists")));
  }

  private @NonNull AccountUser provisionNewAccount(@NonNull AuthenticatedActor actor) {
    final var user = accountUserRepository.create(AccountUser.create(
        actor.identityProvider(),
        actor.externalSubject(),
        actor.email(),
        actor.displayName(),
        actor.avatarUrl()
    ));
    if (!hasPendingWorkspaceInvitation(user)) {
      provisionDefaultWorkspace(user, actor);
    }
    userPreferencesRepository.create(UserPreferences.defaults(user.userId()));
    return user;
  }

  private boolean hasPendingWorkspaceInvitation(@NonNull AccountUser user) {
    final var now = clock.instant();
    return invitationRepository.findAllByInvitedEmail(user.email()).stream()
        .anyMatch(invitation -> invitation.effectiveStatus(now) == PENDING);
  }

  private @NonNull AccountUser synchronizeProviderEmail(
      @NonNull VersionedAggregate<AccountUser> existing,
      @NonNull AuthenticatedActor actor
  ) {
    final var synchronizedUser = existing.aggregate().synchronizeProviderEmail(actor.email());
    return synchronizedUser.equals(existing.aggregate())
        ? existing.aggregate()
        : accountUserRepository.update(existing.withAggregate(synchronizedUser)).aggregate();
  }

  private @NonNull AccountUser requireActiveAccount(@NonNull AccountUser user) {
    if (!user.isActive()) {
      throw new AccountAccessDeniedException("CAP account is disabled");
    }
    return user;
  }

  private void provisionDefaultWorkspace(
      @NonNull AccountUser user,
      @NonNull AuthenticatedActor actor
  ) {
    final var workspace = workspaceRepository.create(Workspace.createDefault(
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
    membershipRepository.create(WorkspaceMembership.createOwner(workspace.workspaceId(), user.userId()));
  }

}
