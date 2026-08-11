package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.administration.application.port.in.WorkspaceLifecycleUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.dto.AccountMembershipResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.mapper.WorkspaceMembershipRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.AccountWorkspaceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.CreateWorkspaceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.dto.UpdateWorkspaceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.mapper.WorkspaceRestMapper;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Thin REST adapter for workspace lifecycle commands. */
@RestController
@RequiredArgsConstructor
public final class WorkspaceLifecycleController implements WorkspaceLifecycleApi {

  private final @NonNull WorkspaceLifecycleUseCase workspaceLifecycleUseCase;

  @Override
  @PostMapping("/account/workspaces")
  public @NonNull ResponseEntity<@NonNull AccountMembershipResponse> createWorkspace(
      @RequestBody @NonNull CreateWorkspaceRequest request
  ) {
    final var membership = workspaceLifecycleUseCase.create(WorkspaceRestMapper.toCommand(request));
    return ResponseEntity.status(201).body(WorkspaceMembershipRestMapper.toResponse(membership));
  }

  @Override
  @PatchMapping("/account/workspaces/{workspaceId}")
  public @NonNull ResponseEntity<@NonNull AccountWorkspaceResponse> updateWorkspace(
      @PathVariable @NonNull UUID workspaceId,
      @RequestBody @NonNull UpdateWorkspaceRequest request
  ) {
    final var updated = workspaceLifecycleUseCase.update(WorkspaceRestMapper.toCommand(
        WorkspaceId.of(workspaceId),
        request
    ));
    return ResponseEntity.ok(WorkspaceRestMapper.toResponse(updated));
  }

  @Override
  @PostMapping("/account/workspaces/{workspaceId}/archive")
  public @NonNull ResponseEntity<@NonNull AccountWorkspaceResponse> archiveWorkspace(
      @PathVariable @NonNull UUID workspaceId
  ) {
    return ResponseEntity.ok(WorkspaceRestMapper.toResponse(
        workspaceLifecycleUseCase.archive(WorkspaceId.of(workspaceId))
    ));
  }
}
