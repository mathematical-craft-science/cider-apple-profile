package org.craft.mcs.cap.infrastructure.configuration;

/** Identity provider selected for JWT validation and identity claim mapping. */
public enum ConfiguredIdentityProvider {
  /** Clerk-hosted identity and session provider. */
  CLERK,
  /** CAP-managed Keycloak OpenID Connect provider. */
  KEYCLOAK
}
