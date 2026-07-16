package org.craft.mcs.cap.infrastructure.adapter.in.rest.account;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.administration.application.port.in.ListWorkspaceMembersUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentAccountProfileUseCase;
import org.craft.mcs.cap.core.account.context.application.port.in.GetCurrentAccountUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentUserPreferencesUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.UpdateAccountProfileUseCase;
import org.craft.mcs.cap.core.account.user.application.port.in.UpdateUserPreferencesUseCase;
import org.craft.mcs.cap.core.account.workspace.application.port.in.GetWorkspaceSettingsUseCase;
import org.craft.mcs.cap.core.account.context.application.port.in.ListAccountWorkspacesUseCase;
import org.craft.mcs.cap.core.account.workspace.application.port.in.UpdateWorkspaceSettingsUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountMeResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountPreferencesResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountUserResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountWorkspaceListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountWorkspaceMemberListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.UpdateAccountPreferencesRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.UpdateAccountProfileRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.UpdateWorkspaceSettingsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.WorkspaceSettingsResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.mapper.AccountRestMapper;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** Thin REST adapter exposing provider-neutral account use cases. */
@RestController
@RequiredArgsConstructor
public final class AccountController implements AccountApi {

  private final @NonNull GetCurrentAccountUseCase getCurrentAccountUseCase;
  private final @NonNull GetCurrentAccountProfileUseCase getCurrentAccountProfileUseCase;
  private final @NonNull GetCurrentUserPreferencesUseCase getCurrentUserPreferencesUseCase;
  private final @NonNull UpdateAccountProfileUseCase updateAccountProfileUseCase;
  private final @NonNull UpdateUserPreferencesUseCase updateUserPreferencesUseCase;
  private final @NonNull ListAccountWorkspacesUseCase listAccountWorkspacesUseCase;
  private final @NonNull ListWorkspaceMembersUseCase listWorkspaceMembersUseCase;
  private final @NonNull GetWorkspaceSettingsUseCase getWorkspaceSettingsUseCase;
  private final @NonNull UpdateWorkspaceSettingsUseCase updateWorkspaceSettingsUseCase;

  @Override
  @GetMapping(value = "/account/me", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull AccountMeResponse> me() {
    return ResponseEntity.ok(AccountRestMapper.toResponse(getCurrentAccountUseCase.get()));
  }

  @Override
  @GetMapping(value = "/account/profile", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull AccountUserResponse> profile() {
    return ResponseEntity.ok(AccountRestMapper.toResponse(getCurrentAccountProfileUseCase.get()));
  }

  @Override
  @PatchMapping(
      value = "/account/profile",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull AccountUserResponse> updateProfile(
      @RequestBody @NonNull UpdateAccountProfileRequest request
  ) {
    return ResponseEntity.ok(AccountRestMapper.toResponse(
        updateAccountProfileUseCase.update(AccountRestMapper.toCommand(request))
    ));
  }

  @Override
  @GetMapping(value = "/account/preferences", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull AccountPreferencesResponse> preferences() {
    return ResponseEntity.ok(AccountRestMapper.toResponse(getCurrentUserPreferencesUseCase.get()));
  }

  @Override
  @PatchMapping(
      value = "/account/preferences",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull AccountPreferencesResponse> updatePreferences(
      @RequestBody @NonNull UpdateAccountPreferencesRequest request
  ) {
    return ResponseEntity.ok(AccountRestMapper.toResponse(
        updateUserPreferencesUseCase.update(AccountRestMapper.toCommand(request))
    ));
  }

  @Override
  @GetMapping(value = "/account/workspaces", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull AccountWorkspaceListResponse> workspaces() {
    return ResponseEntity.ok(new AccountWorkspaceListResponse(
        listAccountWorkspacesUseCase.list().stream().map(AccountRestMapper::toResponse).toList()
    ));
  }

  @Override
  @GetMapping(
      value = "/account/workspaces/{workspaceId}/settings",
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull WorkspaceSettingsResponse> workspaceSettings(
      @PathVariable @NonNull UUID workspaceId
  ) {
    return ResponseEntity.ok(AccountRestMapper.toResponse(
        getWorkspaceSettingsUseCase.get(WorkspaceId.of(workspaceId))
    ));
  }

  @Override
  @PatchMapping(
      value = "/account/workspaces/{workspaceId}/settings",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull WorkspaceSettingsResponse> updateWorkspaceSettings(
      @PathVariable @NonNull UUID workspaceId,
      @RequestBody @NonNull UpdateWorkspaceSettingsRequest request
  ) {
    return ResponseEntity.ok(AccountRestMapper.toResponse(
        updateWorkspaceSettingsUseCase.update(AccountRestMapper.toCommand(
            WorkspaceId.of(workspaceId),
            request
        ))
    ));
  }

  @Override
  @GetMapping(value = "/account/workspaces/{workspaceId}/members", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull AccountWorkspaceMemberListResponse> workspaceMembers(
      @PathVariable @NonNull UUID workspaceId
  ) {
    return ResponseEntity.ok(new AccountWorkspaceMemberListResponse(
        listWorkspaceMembersUseCase.list(WorkspaceId.of(workspaceId)).stream()
            .map(AccountRestMapper::toResponse)
            .toList()
    ));
  }
}
