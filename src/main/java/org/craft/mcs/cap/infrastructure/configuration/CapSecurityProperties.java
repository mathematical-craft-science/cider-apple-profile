package org.craft.mcs.cap.infrastructure.configuration;

import java.net.URI;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import static java.util.Objects.requireNonNull;


/** Provider and public-route settings for CAP resource-server security. */
@ConfigurationProperties(prefix = "cap.security")
public record CapSecurityProperties(
    /* Identity provider used by this CAP application instance. */
    @NonNull ConfiguredIdentityProvider provider,
    /* Clerk OIDC endpoints used to authenticate bearer tokens. */
    @NonNull ClerkProperties clerk,
    /* Keycloak OIDC endpoints and backend audience used to authenticate bearer tokens. */
    @NonNull KeycloakProperties keycloak,
    /* Explicit exceptions to the authenticated-by-default HTTP policy. */
    @NonNull PublicAccessProperties publicAccess
) {

  public CapSecurityProperties {
    requireNonNull(provider, "configured identity provider cannot be null");
    requireNonNull(clerk, "Clerk security properties cannot be null");
    requireNonNull(keycloak, "Keycloak security properties cannot be null");
    requireNonNull(publicAccess, "public access properties cannot be null");
  }

  /** Clerk issuer and JWKS endpoints used exclusively by the security adapter. */
  public record ClerkProperties(
      /* Expected issuer URI of Clerk access tokens. */
      @NonNull URI issuer,
      /* URI from which the resource server loads Clerk JSON Web Keys. */
      @NonNull URI jwkSetUri
  ) {

    public ClerkProperties {
      requireNonNull(issuer, "Clerk issuer cannot be null");
      requireNonNull(jwkSetUri, "Clerk JWKS URI cannot be null");
    }
  }

  /** Keycloak issuer, JWKS endpoint and required access-token audience. */
  public record KeycloakProperties(
      /* Expected issuer URI of Keycloak access tokens. */
      @NonNull URI issuer,
      /* URI from which the resource server loads Keycloak JSON Web Keys. */
      @NonNull URI jwkSetUri,
      /* Audience that identifies the CAP backend resource server. */
      @NonNull String audience
  ) {

    public KeycloakProperties {
      requireNonNull(issuer, "Keycloak issuer cannot be null");
      requireNonNull(jwkSetUri, "Keycloak JWKS URI cannot be null");
      requireNonNull(audience, "Keycloak audience cannot be null");
      audience = audience.strip();
      if (audience.isEmpty()) {
        throw new IllegalArgumentException("Keycloak audience cannot be blank");
      }
    }
  }

  /** Local/dev-only public access toggles for API documentation and debug routes. */
  public record PublicAccessProperties(
      /* Whether OpenAPI and Swagger UI routes are accessible without authentication. */
      boolean openapi,
      /* Whether debug routes are accessible without authentication. */
      boolean debug
  ) {

    public PublicAccessProperties( // NOSONAR
        @DefaultValue("false") boolean openapi,
        @DefaultValue("false") boolean debug
    ) {
      this.openapi = openapi;
      this.debug = debug;
    }
  }
}
