package org.craft.mcs.cap.infrastructure.adapter.in.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.craft.mcs.cap.core.account.membership.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.provisioning.application.port.in.EnsureCurrentAccountProvisionedUseCase;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import static java.util.Objects.requireNonNull;

/** Lazily provisions the CAP user account after successful resource-server authentication. */
@Component
public final class AccountProvisioningFilter extends OncePerRequestFilter {

  private final @NonNull EnsureCurrentAccountProvisionedUseCase ensureCurrentAccountProvisionedUseCase;
  private final @NonNull BearerTokenAuthenticationEntryPoint authenticationEntryPoint =
      new BearerTokenAuthenticationEntryPoint();
  private final @NonNull BearerTokenAccessDeniedHandler accessDeniedHandler =
      new BearerTokenAccessDeniedHandler();

  public AccountProvisioningFilter(
      @NonNull EnsureCurrentAccountProvisionedUseCase ensureCurrentAccountProvisionedUseCase
  ) {
    this.ensureCurrentAccountProvisionedUseCase = requireNonNull(
        ensureCurrentAccountProvisionedUseCase,
        "ensure current account provisioned use case cannot be null"
    );
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    final var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof JwtAuthenticationToken) || !authentication.isAuthenticated()) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      ensureCurrentAccountProvisionedUseCase.ensureProvisioned();
      filterChain.doFilter(request, response);
    } catch (AuthenticationException exception) {
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request, response, exception);
    } catch (AccountAccessDeniedException exception) {
      accessDeniedHandler.handle(
          request,
          response,
          new AccessDeniedException(exception.getMessage(), exception)
      );
    }
  }
}
