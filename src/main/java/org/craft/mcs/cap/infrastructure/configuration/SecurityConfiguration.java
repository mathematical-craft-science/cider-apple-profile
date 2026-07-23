package org.craft.mcs.cap.infrastructure.configuration;

import java.net.URI;
import org.craft.mcs.cap.infrastructure.adapter.in.security.AccountProvisioningFilter;
import org.craft.mcs.cap.infrastructure.adapter.in.security.DebugAccessAuthorizationManager;
import org.craft.mcs.cap.infrastructure.adapter.out.requestcontext.account.oauth2.JwtActorMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.requestcontext.account.oauth2.clerk.ClerkJwtActorMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.requestcontext.account.oauth2.keycloak.KeycloakJwtActorMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;


/**
 * Configures CAP as a stateless OAuth2 resource server and applies public-route policy.
 */
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(CapSecurityProperties.class)
public class SecurityConfiguration {

  @Bean
  public @NonNull JwtActorMapper jwtActorMapper(@NonNull CapSecurityProperties properties) {
    return switch (properties.provider()) {
      case CLERK -> new ClerkJwtActorMapper();
      case KEYCLOAK -> new KeycloakJwtActorMapper();
    };
  }

  @Bean
  public @NonNull JwtDecoder jwtDecoder(@NonNull CapSecurityProperties properties) {
    final var endpoints = switch (properties.provider()) {
      case CLERK -> new ProviderEndpoints(
          properties.clerk().issuer(),
          properties.clerk().jwkSetUri()
      );
      case KEYCLOAK -> new ProviderEndpoints(
          properties.keycloak().issuer(),
          properties.keycloak().jwkSetUri()
      );
    };
    final var decoder = NimbusJwtDecoder
        .withJwkSetUri(endpoints.jwkSetUri().toString())
        .build();
    decoder.setJwtValidator(jwtValidator(properties, endpoints));
    return decoder;
  }

  static @NonNull OAuth2TokenValidator<Jwt> jwtValidator(
      @NonNull CapSecurityProperties properties,
      @NonNull ProviderEndpoints endpoints
  ) {
    final var issuerValidator = JwtValidators.createDefaultWithIssuer(endpoints.issuer().toString());
    return switch (properties.provider()) {
      case CLERK -> issuerValidator;
      case KEYCLOAK -> new DelegatingOAuth2TokenValidator<>(
          issuerValidator,
          new JwtAudienceValidator(properties.keycloak().audience())
      );
    };
  }

  @Bean
  public @NonNull SecurityFilterChain securityFilterChain(
      @NonNull HttpSecurity http,
      @NonNull CapSecurityProperties properties,
      @NonNull AccountProvisioningFilter accountProvisioningFilter,
      @NonNull DebugAccessAuthorizationManager debugAccessAuthorizationManager
  ) {
    http
        .csrf(AbstractHttpConfigurer::disable) // NOSONAR
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        ).authorizeHttpRequests(authorize -> {
              authorize.requestMatchers(
                  "/actuator/health",
                  "/actuator/health/**",
                  "/error"
              ).permitAll();
              final var openApiMatchers = authorize.requestMatchers(
                  "/v3/api-docs",
                  "/v3/api-docs/**",
                  "/swagger-ui.html",
                  "/swagger-ui/**"
              );
              if (properties.publicAccess().openapi()) {
                openApiMatchers.permitAll();
              } else {
                openApiMatchers.authenticated();
              }

              final var debugMatchers = authorize.requestMatchers("/debug/**");
              if (properties.publicAccess().debug()) {
                debugMatchers.permitAll();
              } else {
                debugMatchers.access(debugAccessAuthorizationManager);
              }
              authorize.anyRequest().authenticated();
            }
        ).oauth2ResourceServer(resourceServer ->
            resourceServer.jwt(Customizer.withDefaults())
        ).addFilterAfter(accountProvisioningFilter, BearerTokenAuthenticationFilter.class);

    return http.build();
  }

  record ProviderEndpoints(
      @NonNull URI issuer,
      @NonNull URI jwkSetUri
  ) {
  }
}
