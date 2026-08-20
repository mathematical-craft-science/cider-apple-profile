package org.craft.mcs.cap.infrastructure.adapter.out.requestcontext.account.oauth2.keycloak;

import java.time.Instant;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.HttpUrl;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.user.application.contract.AuthenticatedActor;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeycloakJwtActorMapperTest {

  private final KeycloakJwtActorMapper mapper = new KeycloakJwtActorMapper();

  @Test
  void mapsVerifiedStandardOidcClaimsToProviderNeutralActor() {
    final var actor = mapper.toActor(jwtBuilder()
        .subject("019c6638-721a-7ad4-8492-f0d8052c2c3f")
        .claim("email", "technologist@example.com")
        .claim("email_verified", true)
        .claim("name", "CAP Technologist")
        .claim("picture", "https://example.com/avatar.png")
        .build());

    assertThat(actor)
        .returns(IdentityProviderCode.KEYCLOAK, AuthenticatedActor::identityProvider)
        .returns("019c6638-721a-7ad4-8492-f0d8052c2c3f", value -> value.externalSubject().value())
        .returns(EmailAddress.of("technologist@example.com"), AuthenticatedActor::email)
        .returns(DisplayName.of("CAP Technologist"), AuthenticatedActor::displayName)
        .returns(HttpUrl.of("https://example.com/avatar.png"), AuthenticatedActor::avatarUrl)
        .returns(null, AuthenticatedActor::externalOrganizationSubject);
  }

  @Test
  void usesPreferredUsernameWhenDisplayNameIsAbsent() {
    final var actor = mapper.toActor(jwtBuilder()
        .subject("user-123")
        .claim("email", "technologist@example.com")
        .claim("email_verified", true)
        .claim("preferred_username", "technologist")
        .build());

    assertThat(actor.displayName())
        .isEqualTo(DisplayName.of("technologist"));
  }

  @Test
  void usesEmailLocalPartWhenDisplayNameClaimsAreAbsent() {
    final var actor = mapper.toActor(jwtBuilder()
        .subject("user-123")
        .claim("email", "technologist@example.com")
        .claim("email_verified", true)
        .build());

    assertThat(actor.displayName())
        .isEqualTo(DisplayName.of("technologist"));
  }

  @Test
  void rejectsUnverifiedEmail() {
    final var jwt = jwtBuilder()
        .subject("user-123")
        .claim("email", "technologist@example.com")
        .claim("email_verified", false)
        .build();

    assertThatThrownBy(() -> mapper.toActor(jwt))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Keycloak JWT requires a verified email");
  }

  @Test
  void rejectsTokenWithoutEmailClaim() {
    final var jwt = jwtBuilder()
        .subject("user-123")
        .claim("email_verified", true)
        .build();

    assertThatThrownBy(() -> mapper.toActor(jwt))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Keycloak JWT email claim is required");
  }

  @Test
  void rejectsMalformedIdentityClaimBeforeItReachesAccountDomain() {
    final var jwt = jwtBuilder()
        .subject("user-123")
        .claim("email", "invalid-email")
        .claim("email_verified", true)
        .build();

    assertThatThrownBy(() -> mapper.toActor(jwt))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Keycloak JWT identity claims are invalid");
  }

  private static Jwt.Builder jwtBuilder() {
    final var now = Instant.parse("2026-07-22T00:00:00Z");
    return Jwt.withTokenValue("token")
        .header("alg", "RS256")
        .issuedAt(now)
        .expiresAt(now.plusSeconds(300));
  }
}
