package org.craft.mcs.cap.infrastructure.configuration;

import org.craft.mcs.cap.core.account.provisioning.application.port.in.EnsureCurrentAccountProvisionedUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.security.AccountProvisioningFilter;
import org.craft.mcs.cap.infrastructure.adapter.in.security.DebugAccessAuthorizationManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(OpenApiPublicAccessWebMvcTest.OpenApiStubController.class)
@ImportAutoConfiguration({
    SecurityAutoConfiguration.class,
    ServletWebSecurityAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class
})
@Import({
    SecurityConfiguration.class,
    AccountProvisioningFilter.class,
    OpenApiPublicAccessWebMvcTest.OpenApiStubController.class
})
@ActiveProfiles("local")
class OpenApiPublicAccessWebMvcTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private JwtDecoder jwtDecoder;

  @MockitoBean
  private EnsureCurrentAccountProvisionedUseCase ensureCurrentAccountProvisionedUseCase;

  @MockitoBean
  private DebugAccessAuthorizationManager debugAccessAuthorizationManager;

  @Test
  void permitsOpenApiRootWithoutAuthenticationInLocalProfile() throws Exception {
    mockMvc.perform(get("/v3/api-docs"))
        .andExpect(status().isNoContent());
  }

  @Test
  void permitsGroupedOpenApiPathWithoutAuthenticationInLocalProfile() throws Exception {
    mockMvc.perform(get("/v3/api-docs/cap"))
        .andExpect(status().isNoContent());
  }

  @RestController
  static final class OpenApiStubController {

    @GetMapping({"/v3/api-docs", "/v3/api-docs/{group}"})
    ResponseEntity<Void> apiDocs() {
      return ResponseEntity.noContent().build();
    }
  }
}
