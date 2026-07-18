package org.craft.mcs.cap.core.account.user.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.shared.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.user.application.AccountNotFoundException;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentAccountUserRequestCache;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentActorProvider;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/** Resolves the active CAP user represented by the current authenticated provider identity. */
@Component
@RequiredArgsConstructor
public final class CurrentAccountUserResolver {

  private final @NonNull CurrentActorProvider currentActorProvider;
  private final @NonNull AccountUserRepository accountUserRepository;
  private final @NonNull CurrentAccountUserRequestCache currentAccountUserRequestCache;

  public @NonNull AccountUser resolve() {
    return currentAccountUserRequestCache.current()
        .orElseGet(this::resolveFromIdentity);
  }

  private @NonNull AccountUser resolveFromIdentity() {
    final var actor = currentActorProvider.currentActor()
        .orElseThrow(() -> new AccountAccessDeniedException("authenticated actor is required"));
    final var user = accountUserRepository
        .findByExternalIdentity(actor.identityProvider(), actor.externalSubject())
        .orElseThrow(AccountNotFoundException::new);
    if (!user.isActive()) {
      throw new AccountAccessDeniedException("CAP account is disabled");
    }
    currentAccountUserRequestCache.remember(user);
    return user;
  }
}
