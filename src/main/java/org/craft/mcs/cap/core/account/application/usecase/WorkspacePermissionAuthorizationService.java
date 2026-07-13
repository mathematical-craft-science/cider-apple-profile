package org.craft.mcs.cap.core.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.application.AccountNotFoundException;
import org.craft.mcs.cap.core.account.application.port.in.WorkspacePermissionAuthorizer;
import org.craft.mcs.cap.core.account.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.application.port.out.CurrentActorProvider;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.domain.model.AccountPermission;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Authorizes workspace permissions from persisted memberships without provisioning or writes. */
@Component
@RequiredArgsConstructor
public final class WorkspacePermissionAuthorizationService implements WorkspacePermissionAuthorizer {

  private final @NonNull CurrentActorProvider currentActorProvider;
  private final @NonNull AccountUserRepository accountUserRepository;
  private final @NonNull WorkspaceRepository workspaceRepository;
  private final @NonNull WorkspaceMembershipRepository membershipRepository;

  @Override
  public void requirePermission(
      @NonNull CiderMakerId ciderMakerId,
      @NonNull AccountPermission permission
  ) {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(permission, "account permission cannot be null");

    final var actor = currentActorProvider.currentActor()
        .orElseThrow(() -> new AccountAccessDeniedException("authenticated actor is required"));
    final var user = accountUserRepository
        .findByExternalIdentity(actor.identityProvider(), actor.externalSubject())
        .orElseThrow(AccountNotFoundException::new);
    if (!user.isActive()) {
      throw new AccountAccessDeniedException("CAP account is disabled");
    }

    final var workspace = workspaceRepository.findByCiderMakerId(ciderMakerId)
        .orElseThrow(() -> accessDenied(ciderMakerId));
    final var authorized = membershipRepository
        .findByWorkspaceIdAndUserId(workspace.workspaceId(), user.userId())
        .filter(WorkspaceMembership::isActive)
        .filter(membership -> membership.role().permissions().contains(permission))
        .isPresent();
    if (!authorized) {
      throw accessDenied(ciderMakerId);
    }
  }

  private static @NonNull AccountAccessDeniedException accessDenied(
      @NonNull CiderMakerId ciderMakerId
  ) {
    return new AccountAccessDeniedException(
        "current actor cannot access cider maker scope " + ciderMakerId.value()
    );
  }
}
