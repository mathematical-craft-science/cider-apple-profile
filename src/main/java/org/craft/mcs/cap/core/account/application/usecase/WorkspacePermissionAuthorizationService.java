package org.craft.mcs.cap.core.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.application.port.in.WorkspacePermissionAuthorizer;
import org.craft.mcs.cap.core.account.domain.model.AccountPermission;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Authorizes workspace permissions from persisted memberships without provisioning or writes. */
@Component
@RequiredArgsConstructor
public final class WorkspacePermissionAuthorizationService implements WorkspacePermissionAuthorizer {

  private final @NonNull CurrentWorkspaceContextResolver currentWorkspaceContextResolver;

  @Override
  public void requirePermission(
      @NonNull CiderMakerId ciderMakerId,
      @NonNull AccountPermission permission
  ) {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(permission, "account permission cannot be null");

    final var activeMembership = currentWorkspaceContextResolver.resolve().activeMembership();
    if (!activeMembership.workspace().ciderMakerId().equals(ciderMakerId)
        || !activeMembership.role().permissions().contains(permission)) {
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
