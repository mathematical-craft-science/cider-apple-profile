package org.craft.mcs.cap.account.user.application.usecase;

import java.util.Optional;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.user.application.contract.AuthenticatedActor;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentAccountUserRequestCache;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentActorProvider;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CurrentAccountUserResolverTest {

  @Test
  void returnsAccountRememberedForCurrentRequest() {
    final var actorProvider = mock(CurrentActorProvider.class);
    final var repository = mock(AccountUserRepository.class);
    final var requestCache = mock(CurrentAccountUserRequestCache.class);
    final var user = accountUser();
    when(requestCache.current()).thenReturn(Optional.of(user));

    final var resolver = new CurrentAccountUserResolver(actorProvider, repository, requestCache);

    assertThat(resolver.resolve()).isSameAs(user);
    verifyNoInteractions(actorProvider, repository);
  }

  @Test
  void remembersAccountResolvedFromAuthenticatedIdentity() {
    final var actorProvider = mock(CurrentActorProvider.class);
    final var repository = mock(AccountUserRepository.class);
    final var requestCache = mock(CurrentAccountUserRequestCache.class);
    final var user = accountUser();
    final var actor = new AuthenticatedActor(
        user.identityProvider(),
        user.externalSubject(),
        user.email(),
        user.displayName(),
        user.avatarUrl(),
        null
    );
    when(requestCache.current()).thenReturn(Optional.empty());
    when(actorProvider.currentActor()).thenReturn(Optional.of(actor));
    when(repository.findByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.of(user));

    final var resolver = new CurrentAccountUserResolver(actorProvider, repository, requestCache);

    assertThat(resolver.resolve()).isSameAs(user);
    verify(requestCache).remember(user);
  }

  private static @NonNull AccountUser accountUser() {
    return AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_123"),
        EmailAddress.of("technologist@example.com"),
        DisplayName.of("CAP Technologist"),
        null
    );
  }
}
