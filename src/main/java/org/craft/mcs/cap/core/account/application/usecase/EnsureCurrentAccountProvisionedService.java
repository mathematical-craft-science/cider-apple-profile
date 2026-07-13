package org.craft.mcs.cap.core.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.application.model.AuthenticatedActor;
import org.craft.mcs.cap.core.account.application.model.WorkspaceProvisioningDefaults;
import org.craft.mcs.cap.core.account.application.port.in.EnsureCurrentAccountProvisionedUseCase;
import org.craft.mcs.cap.core.account.application.port.out.AccountProvisioningLock;
import org.craft.mcs.cap.core.account.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.application.port.out.CurrentActorProvider;
import org.craft.mcs.cap.core.account.application.port.out.UserPreferencesRepository;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.core.account.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.domain.model.UserPreferences;
import org.craft.mcs.cap.core.account.domain.model.Workspace;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** Atomically provisions the CAP account and synchronizes its provider-owned email. */
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
    final var user = resolveUser(actor);
    if (!user.isActive()) {
      throw new AccountAccessDeniedException("CAP account is disabled");
    }

    final var memberships = membershipRepository.findAllByUserId(user.userId());
    if (memberships.isEmpty()) {
      createDefaultMembership(user, actor);
    } else if (memberships.stream().noneMatch(WorkspaceMembership::isActive)) {
      throw new AccountAccessDeniedException("account has no active workspace membership");
    }

    userPreferencesRepository.findByUserId(user.userId())
        .orElseGet(() -> userPreferencesRepository.save(UserPreferences.defaults(user.userId()))); // NOSONAR
  }

  private @NonNull AccountUser resolveUser(@NonNull AuthenticatedActor actor) {
    return accountUserRepository.findByExternalIdentity(actor.identityProvider(), actor.externalSubject())
        .map(existing -> synchronizeProviderEmail(existing, actor))
        .orElseGet(() -> accountUserRepository.save(AccountUser.create(
            actor.identityProvider(),
            actor.externalSubject(),
            actor.email(),
            actor.displayName(),
            actor.avatarUrl()
        )));
  }

  private @NonNull AccountUser synchronizeProviderEmail(
      @NonNull AccountUser existing,
      @NonNull AuthenticatedActor actor
  ) {
    final var synchronizedUser = existing.synchronizeProviderEmail(actor.email());
    return synchronizedUser.equals(existing) ? existing : accountUserRepository.save(synchronizedUser);
  }

  private @NonNull WorkspaceMembership createDefaultMembership( // NOSONAR
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
    workspaceSettingsRepository.save(new WorkspaceSettings(
        workspace.workspaceId(),
        settingsDefaults.measurementStaleAfter(),
        settingsDefaults.defaultAcidityBasis()
    ));
    return membershipRepository.save(WorkspaceMembership.createOwner(workspace.workspaceId(), user.userId()));
  }
}
