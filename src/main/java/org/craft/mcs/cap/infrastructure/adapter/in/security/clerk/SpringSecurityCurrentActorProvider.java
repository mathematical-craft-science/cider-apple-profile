package org.craft.mcs.cap.infrastructure.adapter.in.security.clerk;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.model.AuthenticatedActor;
import org.craft.mcs.cap.core.account.application.port.out.CurrentActorProvider;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;


/** Reads the current Spring Security JWT and translates it into the CAP authentication boundary. */
@Component
@RequiredArgsConstructor
public final class SpringSecurityCurrentActorProvider implements CurrentActorProvider {

  private final @NonNull ClerkJwtActorMapper actorMapper;

  @Override
  public @NonNull Optional<@NonNull AuthenticatedActor> currentActor() {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication) || !authentication.isAuthenticated()) {
      return Optional.empty();
    }
    return Optional.of(actorMapper.toActor(jwtAuthentication.getToken()));
  }
}
