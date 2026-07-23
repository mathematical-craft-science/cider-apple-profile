package org.craft.mcs.cap.infrastructure.adapter.out.requestcontext.account.oauth2;

import java.util.Optional;
import org.craft.mcs.cap.core.account.user.application.model.AuthenticatedActor;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentActorProvider;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/** Reads the current Spring Security JWT and translates it through the selected identity provider adapter. */
@Component
public final class SpringSecurityCurrentActorProvider implements CurrentActorProvider {

  private final @NonNull JwtActorMapper actorMapper;

  public SpringSecurityCurrentActorProvider(@NonNull JwtActorMapper actorMapper) {
    this.actorMapper = requireNonNull(actorMapper, "JWT actor mapper cannot be null");
  }

  @Override
  public @NonNull Optional<@NonNull AuthenticatedActor> currentActor() {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication) || !authentication.isAuthenticated()) {
      return Optional.empty();
    }
    return Optional.of(actorMapper.toActor(jwtAuthentication.getToken()));
  }
}
