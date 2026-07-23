package org.craft.mcs.cap.infrastructure.adapter.out.requestcontext.account.oauth2;

import org.craft.mcs.cap.core.account.user.application.model.AuthenticatedActor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.jwt.Jwt;

/** Anti-corruption boundary that maps provider-specific JWT claims to a CAP authenticated actor. */
@FunctionalInterface
public interface JwtActorMapper {

  @NonNull AuthenticatedActor toActor(@NonNull Jwt jwt);
}
