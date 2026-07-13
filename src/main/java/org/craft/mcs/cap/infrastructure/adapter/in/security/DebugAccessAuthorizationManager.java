package org.craft.mcs.cap.infrastructure.adapter.in.security;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.port.in.GetCurrentAccountUseCase;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.domain.model.AccountRole;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;


/** Grants non-public debug routes only to active CAP workspace owners and administrators. */
@Component
@RequiredArgsConstructor
public final class DebugAccessAuthorizationManager
    implements AuthorizationManager<RequestAuthorizationContext> {

  private final @NonNull GetCurrentAccountUseCase getCurrentAccountUseCase;

  @Override
  public @NonNull AuthorizationResult authorize(
      @NonNull Supplier<? extends Authentication> authentication,
      @NonNull RequestAuthorizationContext context
  ) {
    final var currentAuthentication = authentication.get();
    if (currentAuthentication == null || !currentAuthentication.isAuthenticated()) {
      return new AuthorizationDecision(false);
    }

    final var allowed = getCurrentAccountUseCase.get().memberships().stream()
        .map(WorkspaceMembershipView::role)
        .anyMatch(role -> role == AccountRole.OWNER || role == AccountRole.ADMIN);
    return new AuthorizationDecision(allowed);
  }
}
