package org.craft.mcs.cap.infrastructure.adapter.out.requestcontext.account.oauth2.keycloak;

import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.user.application.contract.AuthenticatedActor;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.infrastructure.adapter.out.requestcontext.account.oauth2.JwtActorMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.requestcontext.account.oauth2.JwtIdentityClaimReader;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

import static java.util.Objects.requireNonNull;

/** Anti-corruption mapper from standard Keycloak OIDC claims to a provider-neutral CAP actor. */
public final class KeycloakJwtActorMapper implements JwtActorMapper {

  private final @NonNull JwtIdentityClaimReader claims = new JwtIdentityClaimReader("Keycloak");

  @Override
  public @NonNull AuthenticatedActor toActor(@NonNull Jwt jwt) {
    requireNonNull(jwt, "JWT cannot be null");
    if (!Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"))) {
      throw new BadCredentialsException("Keycloak JWT requires a verified email");
    }

    final var subject = claims.requiredString(jwt.getSubject(), "Keycloak JWT subject is required");
    final var email = claims.requiredString(
        claims.firstString(jwt, "email"),
        "Keycloak JWT email claim is required"
    );
    final var externalSubject = claims.requiredIdentityValue(() -> ExternalUserSubject.of(subject));
    final var emailAddress = claims.requiredIdentityValue(() -> EmailAddress.of(email));
    final var displayName = claims.firstString(jwt, "name", "preferred_username");
    final var actorDisplayName = claims.requiredIdentityValue(() -> DisplayName.of(
        displayName == null ? emailAddress.localPart() : displayName
    ));

    return new AuthenticatedActor(
        IdentityProviderCode.KEYCLOAK,
        externalSubject,
        emailAddress,
        actorDisplayName,
        claims.identityValue(() -> HttpUrl.fromNullable(claims.firstString(jwt, "picture"))),
        null
    );
  }
}
