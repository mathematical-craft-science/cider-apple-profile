package org.craft.mcs.cap.infrastructure.adapter.in.rest.account;

import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.craft.mcs.cap.core.account.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.application.port.in.AccountMeView;
import org.craft.mcs.cap.core.account.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.application.port.in.EnsureCurrentAccountProvisionedUseCase;
import org.craft.mcs.cap.core.account.application.port.in.GetCurrentAccountUseCase;
import org.craft.mcs.cap.core.account.application.port.in.ListAccountWorkspacesUseCase;
import org.craft.mcs.cap.core.account.application.port.in.ListWorkspaceMembersUseCase;
import org.craft.mcs.cap.core.account.application.port.in.UpdateAccountProfileUseCase;
import org.craft.mcs.cap.core.account.application.port.in.UpdateUserPreferencesUseCase;
import org.craft.mcs.cap.core.account.application.port.in.UserPreferencesView;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceView;
import org.craft.mcs.cap.core.account.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.domain.model.AccountUserStatus;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceMembershipStatus;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceType;
import org.craft.mcs.cap.core.account.domain.model.value.AccountUserId;
import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.craft.mcs.cap.core.account.domain.model.value.EmailAddress;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceId;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.AppleVarietyJsonSeedImporter;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.application.ApplicationExceptionHandler;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.seed.SeedDebugController;
import org.craft.mcs.cap.infrastructure.adapter.in.security.AccountProvisioningFilter;
import org.craft.mcs.cap.infrastructure.adapter.in.security.DebugAccessAuthorizationManager;
import org.craft.mcs.cap.infrastructure.configuration.SecurityConfiguration;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest({AccountController.class, SeedDebugController.class})
@ImportAutoConfiguration({
    SecurityAutoConfiguration.class,
    ServletWebSecurityAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class
})
@Import({
    SecurityConfiguration.class,
    AccountProvisioningFilter.class,
    DebugAccessAuthorizationManager.class,
    ApplicationExceptionHandler.class
})
@TestPropertySource(properties = {
    "cap.security.clerk.issuer=https://example.clerk.accounts.dev",
    "cap.security.clerk.jwk-set-uri=https://example.clerk.accounts.dev/.well-known/jwks.json",
    "cap.security.public-access.openapi=false",
    "cap.security.public-access.debug=false"
})
@ActiveProfiles("local")
class AccountSecurityWebMvcTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private JwtDecoder jwtDecoder;

  @MockitoBean
  private EnsureCurrentAccountProvisionedUseCase ensureCurrentAccountProvisionedUseCase;

  @MockitoBean
  private GetCurrentAccountUseCase getCurrentAccountUseCase;

  @MockitoBean
  private UpdateAccountProfileUseCase updateAccountProfileUseCase;

  @MockitoBean
  private UpdateUserPreferencesUseCase updateUserPreferencesUseCase;

  @MockitoBean
  private ListAccountWorkspacesUseCase listAccountWorkspacesUseCase;

  @MockitoBean
  private ListWorkspaceMembersUseCase listWorkspaceMembersUseCase;

  @MockitoBean
  private AppleVarietyJsonSeedImporter appleVarietyJsonSeedImporter;

  @Test
  void rejectsUnauthenticatedBusinessRequest() throws Exception {
    mockMvc.perform(get("/account/me"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void returnsCurrentCapAccountForAuthenticatedJwt() throws Exception {
    final var account = account(AccountRole.OWNER);
    when(getCurrentAccountUseCase.get()).thenReturn(account);

    mockMvc.perform(get("/account/me").with(jwt().jwt(token -> token
            .subject("user_123")
            .claim("email", "technologist@example.com"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.email").value("technologist@example.com"))
        .andExpect(jsonPath("$.activeWorkspace.name").value("My cidery"))
        .andExpect(jsonPath("$.memberships[0].role").value("OWNER"));
  }

  @Test
  void returnsForbiddenWhenAuthenticatedActorHasNoActiveCapAccountContext() throws Exception {
    when(getCurrentAccountUseCase.get())
        .thenThrow(new AccountAccessDeniedException("account has no active workspace membership"));

    mockMvc.perform(get("/account/me").with(jwt()))
        .andExpect(status().isForbidden());
  }

  @Test
  void returnsForbiddenWhenActorCannotReadWorkspaceMembers() throws Exception {
    final var account = account(AccountRole.VIEWER);
    when(getCurrentAccountUseCase.get()).thenReturn(account);
    when(listWorkspaceMembersUseCase.list(any()))
        .thenThrow(new AccountAccessDeniedException("workspace member list is not accessible"));

    mockMvc.perform(get("/account/workspaces/{workspaceId}/members", UUID.randomUUID())
            .with(jwt().jwt(token -> token.subject("user_123"))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.detail").value("workspace member list is not accessible"));
  }

  @Test
  void grantsProtectedDebugEndpointToWorkspaceAdministrator() throws Exception {
    when(getCurrentAccountUseCase.get()).thenReturn(account(AccountRole.ADMIN));

    mockMvc.perform(post("/debug/seed/apple-varieties").with(jwt()))
        .andExpect(status().isOk());
  }

  @Test
  void deniesProtectedDebugEndpointToTechnologist() throws Exception {
    when(getCurrentAccountUseCase.get()).thenReturn(account(AccountRole.TECHNOLOGIST));

    mockMvc.perform(post("/debug/seed/apple-varieties").with(jwt()))
        .andExpect(status().isForbidden());
  }

  private static AccountMeView account(AccountRole role) {
    final var userId = AccountUserId.newId();
    final var workspace = new WorkspaceView(
        WorkspaceId.newId(),
        CiderMakerId.of(UUID.randomUUID()),
        DisplayName.of("My cidery"),
        WorkspaceType.CIDERY
    );
    final var membership = new WorkspaceMembershipView(
        WorkspaceMembershipId.newId(),
        workspace,
        role,
        WorkspaceMembershipStatus.ACTIVE
    );
    return new AccountMeView(
        new AccountUserView(
            userId,
            EmailAddress.of("technologist@example.com"),
            DisplayName.of("CAP Technologist"),
            null,
            AccountUserStatus.ACTIVE
        ),
        workspace,
        List.of(membership),
        new UserPreferencesView(Locale.ENGLISH, ZoneId.of("UTC")),
        role.permissions()
    );
  }
}
