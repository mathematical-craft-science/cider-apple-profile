package org.craft.mcs.cap.infrastructure.adapter.in.rest.account;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import org.craft.mcs.cap.core.account.shared.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.context.application.ActiveWorkspaceRequiredException;
import org.craft.mcs.cap.core.account.administration.application.port.in.ListWorkspaceMembersUseCase;
import org.craft.mcs.cap.core.account.administration.application.port.in.WorkspaceMembershipManagementUseCase;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipStatus;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.core.account.provisioning.application.port.in.EnsureCurrentAccountProvisionedUseCase;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.context.application.port.in.AccountMeView;
import org.craft.mcs.cap.core.account.user.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentAccountProfileUseCase;
import org.craft.mcs.cap.core.account.context.application.port.in.GetCurrentAccountUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentUserPreferencesUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.UpdateAccountProfileUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.UpdateUserPreferencesUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.UserPreferencesView;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUserStatus;
import org.craft.mcs.cap.core.account.workspace.application.model.WorkspaceSettingsRevision;
import org.craft.mcs.cap.core.account.workspace.application.port.in.GetWorkspaceSettingsUseCase;
import org.craft.mcs.cap.core.account.context.application.port.in.ListAccountWorkspacesUseCase;
import org.craft.mcs.cap.core.account.workspace.application.port.in.UpdateWorkspaceSettingsUseCase;
import org.craft.mcs.cap.core.account.workspace.application.model.WorkspaceSettingsView;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceView;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceStatus;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.AppleVarietyJsonSeedImporter;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.context.AccountContextController;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.WorkspaceMembershipController;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.user.AccountUserController;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.WorkspaceSettingsController;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.application.ApplicationExceptionHandler;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.seed.SeedDebugController;
import org.craft.mcs.cap.infrastructure.adapter.in.security.AccountProvisioningFilter;
import org.craft.mcs.cap.infrastructure.adapter.in.security.DebugAccessAuthorizationManager;
import org.craft.mcs.cap.infrastructure.configuration.SecurityConfiguration;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
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

import static org.craft.mcs.cap.core.account.user.domain.model.PersonalAccountCapability.forActiveAccount;
import static org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis.MALIC_ACID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest({
    AccountContextController.class,
    AccountUserController.class,
    WorkspaceSettingsController.class,
    WorkspaceMembershipController.class,
    SeedDebugController.class
})
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
  private GetCurrentAccountProfileUseCase getCurrentAccountProfileUseCase;

  @MockitoBean
  private GetCurrentUserPreferencesUseCase getCurrentUserPreferencesUseCase;

  @MockitoBean
  private UpdateAccountProfileUseCase updateAccountProfileUseCase;

  @MockitoBean
  private UpdateUserPreferencesUseCase updateUserPreferencesUseCase;

  @MockitoBean
  private ListAccountWorkspacesUseCase listAccountWorkspacesUseCase;

  @MockitoBean
  private ListWorkspaceMembersUseCase listWorkspaceMembersUseCase;

  @MockitoBean
  private WorkspaceMembershipManagementUseCase workspaceMembershipManagementUseCase;

  @MockitoBean
  private GetWorkspaceSettingsUseCase getWorkspaceSettingsUseCase;

  @MockitoBean
  private UpdateWorkspaceSettingsUseCase updateWorkspaceSettingsUseCase;

  @MockitoBean
  private AppleVarietyJsonSeedImporter appleVarietyJsonSeedImporter;

  @Test
  void rejectsUnauthenticatedBusinessRequest() throws Exception {
    mockMvc.perform(get("/account/me"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void protectsOpenApiRoutesWhenPublicAccessIsDisabled() throws Exception {
    mockMvc.perform(get("/v3/api-docs"))
        .andExpect(status().isUnauthorized());
    mockMvc.perform(get("/v3/api-docs/cap"))
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
        .andExpect(jsonPath("$.memberships[0].role").value("OWNER"))
        .andExpect(jsonPath("$.personalCapabilities").isArray())
        .andExpect(jsonPath("$.workspacePermissions").isArray())
        .andExpect(jsonPath("$.permissions").doesNotExist());
  }

  @Test
  void returnsForbiddenWhenAuthenticatedActorHasNoActiveCapAccountContext() throws Exception {
    when(getCurrentAccountUseCase.get())
        .thenThrow(new AccountAccessDeniedException("account has no active workspace membership"));

    mockMvc.perform(get("/account/me").with(jwt()))
        .andExpect(status().isForbidden());
  }

  @Test
  void requiresExplicitWorkspaceForMultiWorkspaceAccount() throws Exception {
    when(getCurrentAccountUseCase.get()).thenThrow(new ActiveWorkspaceRequiredException());

    mockMvc.perform(get("/account/me").with(jwt()))
        .andExpect(status().is(422))
        .andExpect(jsonPath("$.detail")
            .value("active workspace selection is required when account has multiple active workspaces"));
  }

  @Test
  void listsAvailableWorkspacesBeforeActiveWorkspaceSelection() throws Exception {
    final var account = account(AccountRole.OWNER);
    when(listAccountWorkspacesUseCase.list()).thenReturn(account.memberships());

    mockMvc.perform(get("/account/workspaces").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.memberships[0].workspace.name").value("My cidery"));
  }

  @Test
  void returnsPersonalProfileWithoutActiveWorkspaceSelection() throws Exception {
    final var account = account(AccountRole.OWNER);
    when(getCurrentAccountProfileUseCase.get()).thenReturn(account.user());

    mockMvc.perform(get("/account/profile").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.displayName").value("CAP Technologist"));
  }

  @Test
  void returnsEmptyWorkspaceListAfterLastActiveMembershipWasLost() throws Exception {
    when(listAccountWorkspacesUseCase.list()).thenReturn(List.of());

    mockMvc.perform(get("/account/workspaces").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.memberships").isEmpty());
  }

  @Test
  void returnsForbiddenWhenActorCannotReadWorkspaceMembers() throws Exception {
    final var account = account(AccountRole.VIEWER);
    when(getCurrentAccountUseCase.get()).thenReturn(account);
    when(listWorkspaceMembersUseCase.list(any()))
        .thenThrow(new AccountAccessDeniedException("workspace member list is not accessible"));

    mockMvc.perform(get("/account/workspaces/{workspaceId}/members", WorkspaceId.newId().value())
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

  @Test
  void returnsAuditableWorkspaceSettings() throws Exception {
    final var workspaceId = WorkspaceId.newId();
    when(getWorkspaceSettingsUseCase.get(workspaceId)).thenReturn(settings(workspaceId, 3));

    mockMvc.perform(get("/account/workspaces/{workspaceId}/settings", workspaceId.value()).with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.workspaceId").value(workspaceId.value().toString()))
        .andExpect(jsonPath("$.measurementStaleAfter").value("PT336H"))
        .andExpect(jsonPath("$.defaultAcidityBasis").value("MALIC_ACID"))
        .andExpect(jsonPath("$.revision").value(3))
        .andExpect(jsonPath("$.createdAt").value("2026-07-01T00:00:00Z"))
        .andExpect(jsonPath("$.updatedAt").value("2026-07-14T00:00:00Z"));
  }

  @Test
  void patchesWorkspaceSettingsUsingExpectedRevision() throws Exception {
    final var workspaceId = WorkspaceId.newId();
    when(updateWorkspaceSettingsUseCase.update(any())).thenReturn(settings(workspaceId, 4));

    mockMvc.perform(patch("/account/workspaces/{workspaceId}/settings", workspaceId.value())
            .with(jwt())
            .contentType(APPLICATION_JSON)
            .content("""
                {
                  "expectedRevision": 3,
                  "measurementStaleAfter": "PT504H"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.revision").value(4));
  }

  private static @NonNull AccountMeView account(@NonNull AccountRole role) {
    final var userId = AccountUserId.newId();
    final var workspace = new WorkspaceView(
        WorkspaceId.newId(),
        DisplayName.of("My cidery"),
        WorkspaceType.CIDERY,
        WorkspaceStatus.ACTIVE
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
        forActiveAccount(),
        role.workspacePermissions()
    );
  }

  private static @NonNull WorkspaceSettingsView settings(
      @NonNull WorkspaceId workspaceId,
      long revision
  ) {
    return new WorkspaceSettingsView(
        workspaceId,
        Duration.ofDays(14),
        MALIC_ACID,
        WorkspaceSettingsRevision.of(revision),
        Instant.parse("2026-07-01T00:00:00Z"),
        Instant.parse("2026-07-14T00:00:00Z")
    );
  }
}
