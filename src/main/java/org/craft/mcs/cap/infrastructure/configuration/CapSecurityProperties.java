package org.craft.mcs.cap.infrastructure.configuration;

import java.net.URI;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import static java.util.Objects.requireNonNull;


/** Provider and public-route settings for CAP resource-server security. */
@ConfigurationProperties(prefix = "cap.security")
public record CapSecurityProperties(
    /* Clerk OIDC endpoints used to authenticate bearer tokens. */
    @NonNull ClerkProperties clerk,
    /* Explicit exceptions to the authenticated-by-default HTTP policy. */
    @NonNull PublicAccessProperties publicAccess
) {

  public CapSecurityProperties {
    requireNonNull(clerk, "Clerk security properties cannot be null");
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
