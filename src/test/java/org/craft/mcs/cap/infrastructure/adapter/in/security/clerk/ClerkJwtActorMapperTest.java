package org.craft.mcs.cap.infrastructure.adapter.in.security.clerk;

import java.time.Instant;
import org.craft.mcs.cap.core.account.application.model.AuthenticatedActor;
import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.craft.mcs.cap.core.account.domain.model.value.EmailAddress;
import org.craft.mcs.cap.core.account.domain.model.value.HttpUrl;
import org.craft.mcs.cap.core.account.domain.model.value.IdentityProviderCode;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ClerkJwtActorMapperTest {

  private final ClerkJwtActorMapper mapper = new ClerkJwtActorMapper();

  @Test
  void mapsClerkClaimsToProviderNeutralActor() {
    final var actor = mapper.toActor(jwtBuilder()
        .subject("user_123")
        .claim("email", "technologist@example.com")
        .claim("name", "CAP Technologist")
        .claim("image_url", "https://example.com/avatar.png")
        .claim("org_id", "org_123")
        .build());

    assertThat(actor)
        .returns(IdentityProviderCode.CLERK, AuthenticatedActor::identityProvider)
        .returns("user_123", value -> value.externalSubject().value())
        .returns(EmailAddress.of("technologist@example.com"), AuthenticatedActor::email)
        .returns(DisplayName.of("CAP Technologist"), AuthenticatedActor::displayName)
        .returns(HttpUrl.of("https://example.com/avatar.png"), AuthenticatedActor::avatarUrl)
        .returns("org_123", value -> value.externalOrganizationSubject().value());
  }

  @Test
  void rejectsTokenWithoutEmailClaimRequiredForAccountProvisioning() {
    final var jwt = jwtBuilder().subject("user_123").build();

    assertThatThrownBy(() -> mapper.toActor(jwt))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Clerk JWT email claim is required");
  }

  @Test
  void rejectsMalformedIdentityClaimBeforeItReachesAccountDomain() {
    final var jwt = jwtBuilder()
        .subject("user_123")
        .claim("email", "invalid-email")
        .build();

    assertThatThrownBy(() -> mapper.toActor(jwt))
        .isInstanceOf(BadCredentialsException.class)
        .hasMessage("Clerk JWT identity claims are invalid");
  }

  private static Jwt.Builder jwtBuilder() {
    final var now = Instant.parse("2026-07-13T00:00:00Z");
    return Jwt.withTokenValue("token")
        .header("alg", "RS256")
        .issuedAt(now)
        .expiresAt(now.plusSeconds(300));
  }
}
