package org.craft.mcs.cap.core.account.application.usecase;

import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.application.AccountNotFoundException;
import org.craft.mcs.cap.core.account.application.WorkspaceNotFoundException;
import org.craft.mcs.cap.core.account.application.port.in.AccountMeView;
import org.craft.mcs.cap.core.account.application.port.in.EnsureCurrentAccountProvisionedUseCase;
import org.craft.mcs.cap.core.account.application.port.in.GetCurrentAccountUseCase;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.application.port.out.CurrentActorProvider;
import org.craft.mcs.cap.core.account.application.port.out.UserPreferencesRepository;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.domain.model.UserPreferences;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceMembership;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/**
 * Reads the current CAP account after authentication-boundary provisioning.
 *
 * <p>This query never creates or synchronizes account state. Provisioning belongs exclusively to
 * {@link EnsureCurrentAccountProvisionedUseCase} at the security boundary.
 */
@Component
@RequiredArgsConstructor
public final class GetCurrentAccountService implements GetCurrentAccountUseCase {

  private final @NonNull CurrentActorProvider currentActorProvider;
  private final @NonNull AccountUserRepository accountUserRepository;
  private final @NonNull WorkspaceRepository workspaceRepository;
  private final @NonNull WorkspaceMembershipRepository membershipRepository;
  private final @NonNull UserPreferencesRepository userPreferencesRepository;

  @Override
  public @NonNull AccountMeView get() {
    final var actor = currentActorProvider.currentActor()
        .orElseThrow(() -> new AccountAccessDeniedException("authenticated actor is required"));

    final var user = accountUserRepository
        .findByExternalIdentity(actor.identityProvider(), actor.externalSubject())
        .orElseThrow(AccountNotFoundException::new);
    if (!user.isActive()) {
      throw new AccountAccessDeniedException("CAP account is disabled");
    }

    final var memberships = membershipRepository.findAllByUserId(user.userId()).stream()
        .filter(WorkspaceMembership::isActive)
        .toList();
    if (memberships.isEmpty()) {
      throw new AccountAccessDeniedException("account has no active workspace membership");
    }
    final var preferences = userPreferencesRepository.findByUserId(user.userId())
        .orElseGet(() -> UserPreferences.defaults(user.userId()));
    final var membershipViews = memberships.stream()
        .map(this::toView)
        .sorted(Comparator.comparing(view -> view.workspace().name().value()))
        .toList();
    final var activeMembership = membershipViews.getFirst();

    return new AccountMeView(
        AccountViewMapper.toView(user),
        activeMembership.workspace(),
        membershipViews,
        AccountViewMapper.toView(preferences),
        activeMembership.role().permissions()
    );
  }

  private @NonNull WorkspaceMembershipView toView(@NonNull WorkspaceMembership membership) {
    final var workspace = workspaceRepository.findById(membership.workspaceId())
        .orElseThrow(() -> new WorkspaceNotFoundException(membership.workspaceId()));
    return AccountViewMapper.toView(membership, workspace);
  }
}
