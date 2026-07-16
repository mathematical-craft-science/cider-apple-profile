package org.craft.mcs.cap.infrastructure.adapter.in.security;

import org.craft.mcs.cap.core.account.provisioning.application.port.in.EnsureCurrentAccountProvisionedUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountProvisioningFilterTest {

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void provisionsAuthenticatedJwtActorBeforeContinuingRequest() throws Exception {
    final var provisioning = mock(EnsureCurrentAccountProvisionedUseCase.class);
    final var filter = new AccountProvisioningFilter(provisioning);
    final var chain = new MockFilterChain();
    final var request = new MockHttpServletRequest();
    final var response = new MockHttpServletResponse();
    final var authentication = mock(JwtAuthenticationToken.class);
    when(authentication.isAuthenticated()).thenReturn(true);
    final var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);

    filter.doFilterInternal(request, response, chain);

    verify(provisioning).ensureProvisioned();
  }
}
