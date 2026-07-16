package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.workspace.application.port.in.GetWorkspaceSettingsUseCase;
import org.craft.mcs.cap.core.account.workspace.application.port.in.UpdateWorkspaceSettingsUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.UpdateWorkspaceSettingsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.WorkspaceSettingsResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.mapper.WorkspaceRestMapper;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** Thin REST adapter for versioned workspace settings. */
@RestController
@RequiredArgsConstructor
public final class WorkspaceSettingsController implements WorkspaceSettingsApi {

  private final @NonNull GetWorkspaceSettingsUseCase getWorkspaceSettingsUseCase;
  private final @NonNull UpdateWorkspaceSettingsUseCase updateWorkspaceSettingsUseCase;

  @Override
  @GetMapping(
      value = "/account/workspaces/{workspaceId}/settings",
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull WorkspaceSettingsResponse> workspaceSettings(
      @PathVariable @NonNull UUID workspaceId
  ) {
    return ResponseEntity.ok(WorkspaceRestMapper.toResponse(
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
    return ResponseEntity.ok(WorkspaceRestMapper.toResponse(
        updateWorkspaceSettingsUseCase.update(WorkspaceRestMapper.toCommand(
            WorkspaceId.of(workspaceId),
            request
        ))
    ));
  }
}
