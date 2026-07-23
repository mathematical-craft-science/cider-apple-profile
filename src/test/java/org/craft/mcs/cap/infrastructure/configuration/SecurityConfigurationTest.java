package org.craft.mcs.cap.infrastructure.configuration;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.infrastructure.adapter.out.requestcontext.account.oauth2.clerk.ClerkJwtActorMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.requestcontext.account.oauth2.keycloak.KeycloakJwtActorMapper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigurationTest {

  private static final URI CLERK_ISSUER = URI.create("https://example.clerk.accounts.dev");
  private static final URI CLERK_JWKS = CLERK_ISSUER.resolve("/.well-known/jwks.json");
  private static final URI KEYCLOAK_ISSUER = URI.create("https://auth.cap.example/realms/cap");
  private static final URI KEYCLOAK_JWKS = URI.create(
      "https://auth.cap.example/realms/cap/protocol/openid-connect/certs"
  );

  private final SecurityConfiguration configuration = new SecurityConfiguration();

  @Test
  void selectsClerkMapperWithoutAudienceRequirement() {
    final var properties = properties(ConfiguredIdentityProvider.CLERK);
    final var validator = SecurityConfiguration.jwtValidator(
        properties,
        new SecurityConfiguration.ProviderEndpoints(CLERK_ISSUER, CLERK_JWKS)
    );

    assertThat(configuration.jwtActorMapper(properties))
        .isInstanceOf(ClerkJwtActorMapper.class);
    assertThat(configuration.jwtDecoder(properties))
        .isInstanceOf(NimbusJwtDecoder.class);
    assertThat(validator.validate(jwt(CLERK_ISSUER, List.of())))
        .matches(result -> !result.hasErrors());
  }

  @Test
  void selectsKeycloakMapperAndRequiresBackendAudience() {
    final var properties = properties(ConfiguredIdentityProvider.KEYCLOAK);
    final var validator = SecurityConfiguration.jwtValidator(
        properties,
        new SecurityConfiguration.ProviderEndpoints(KEYCLOAK_ISSUER, KEYCLOAK_JWKS)
    );

    assertThat(configuration.jwtActorMapper(properties))
        .isInstanceOf(KeycloakJwtActorMapper.class);
    assertThat(configuration.jwtDecoder(properties))
        .isInstanceOf(NimbusJwtDecoder.class);
    assertThat(validator.validate(jwt(KEYCLOAK_ISSUER, List.of("cap-backend"))))
        .matches(result -> !result.hasErrors());
    assertThat(validator.validate(jwt(KEYCLOAK_ISSUER, List.of("another-service"))))
        .matches(OAuth2TokenValidatorResult::hasErrors);
    assertThat(validator.validate(jwt(URI.create("https://foreign-issuer.example"), List.of("cap-backend"))))
        .matches(OAuth2TokenValidatorResult::hasErrors);
  }

  private static CapSecurityProperties properties(@NonNull ConfiguredIdentityProvider provider) {
    return new CapSecurityProperties(
        provider,
        new CapSecurityProperties.ClerkProperties(CLERK_ISSUER, CLERK_JWKS),
        new CapSecurityProperties.KeycloakProperties(KEYCLOAK_ISSUER, KEYCLOAK_JWKS, "cap-backend"),
        new CapSecurityProperties.PublicAccessProperties(false, false)
    );
  }

  private static Jwt jwt(
      @NonNull URI issuer,
      @NonNull List<@NonNull String> audience
  ) {
    return Jwt.withTokenValue("token")
        .header("alg", "RS256")
        .issuer(issuer.toString())
        .audience(audience)
        .issuedAt(Instant.parse("2026-07-22T00:00:00Z"))
        .expiresAt(Instant.parse("2099-01-01T00:00:00Z"))
        .build();
  }
}
