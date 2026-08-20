package org.craft.mcs.cap.core.account.context.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.context.application.contract.ResolvedWorkspaceContext;
import org.craft.mcs.cap.core.account.context.application.port.out.ActiveWorkspaceProvider;
import org.craft.mcs.cap.core.account.context.application.port.out.CurrentWorkspaceContextRequestCache;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/** Resolves and verifies the active workspace selected for the current authenticated account. */
@Component
@RequiredArgsConstructor
public final class CurrentWorkspaceContextResolver {

  private final @NonNull CurrentAccountUserResolver currentAccountUserResolver;
  private final @NonNull CurrentAccountMembershipReader currentAccountMembershipReader;
  private final @NonNull ActiveWorkspaceProvider activeWorkspaceProvider;
  private final @NonNull ActiveWorkspaceSelectionPolicy activeWorkspaceSelectionPolicy;
  private final @NonNull CurrentWorkspaceContextRequestCache currentWorkspaceContextRequestCache;

  public @NonNull ResolvedWorkspaceContext resolve() {
    return currentWorkspaceContextRequestCache.current()
        .orElseGet(this::resolveAndRemember);
  }

  private @NonNull ResolvedWorkspaceContext resolveAndRemember() {
    final var user = currentAccountUserResolver.resolve();
    final var memberships = currentAccountMembershipReader.readActive(user.userId());
    final var selectedWorkspaceId = activeWorkspaceProvider.selectedWorkspaceId().orElse(null); // NOSONAR
    final var activeMembership = activeWorkspaceSelectionPolicy.select(memberships, selectedWorkspaceId);
    final var context = new ResolvedWorkspaceContext(user, memberships, activeMembership);
    currentWorkspaceContextRequestCache.remember(context);
    return context;
  }
}
