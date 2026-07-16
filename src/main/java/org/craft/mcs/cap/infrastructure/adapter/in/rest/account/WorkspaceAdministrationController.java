package org.craft.mcs.cap.infrastructure.adapter.in.rest.account;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.audit.application.port.in.ListWorkspaceAdministrativeAuditUseCase;
import org.craft.mcs.cap.core.account.administration.application.port.in.WorkspaceLifecycleUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountMembershipResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.AccountWorkspaceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.CreateWorkspaceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.UpdateWorkspaceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.dto.WorkspaceAdministrativeAuditListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.mapper.AccountRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.mapper.WorkspaceAdministrationRestMapper;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Thin REST adapter for workspace lifecycle and administrative audit use cases. */
@RestController
@RequiredArgsConstructor
public final class WorkspaceAdministrationController implements WorkspaceAdministrationApi {

  private final @NonNull WorkspaceLifecycleUseCase workspaceLifecycleUseCase;
  private final @NonNull ListWorkspaceAdministrativeAuditUseCase listWorkspaceAuditUseCase;

  @Override
  @PostMapping("/account/workspaces")
  public @NonNull ResponseEntity<@NonNull AccountMembershipResponse> createWorkspace(
      @Valid @RequestBody @NonNull CreateWorkspaceRequest request
  ) {
    final var membership = workspaceLifecycleUseCase.create(
        WorkspaceAdministrationRestMapper.toCommand(request)
    );
    return ResponseEntity.status(201).body(AccountRestMapper.toResponse(membership));
  }

  @Override
  @PatchMapping("/account/workspaces/{workspaceId}")
  public @NonNull ResponseEntity<@NonNull AccountWorkspaceResponse> updateWorkspace(
      @PathVariable @NonNull UUID workspaceId,
      @Valid @RequestBody @NonNull UpdateWorkspaceRequest request
  ) {
    final var updated = workspaceLifecycleUseCase.update(WorkspaceAdministrationRestMapper.toCommand(
        WorkspaceId.of(workspaceId),
        request
    ));
    return ResponseEntity.ok(AccountRestMapper.toResponse(updated));
  }

  @Override
  @PostMapping("/account/workspaces/{workspaceId}/archive")
  public @NonNull ResponseEntity<@NonNull AccountWorkspaceResponse> archiveWorkspace(
      @PathVariable @NonNull UUID workspaceId
  ) {
    final var archived = workspaceLifecycleUseCase.archive(WorkspaceId.of(workspaceId));
    return ResponseEntity.ok(AccountRestMapper.toResponse(archived));
  }

  @Override
  @GetMapping("/account/workspaces/{workspaceId}/audit")
  public @NonNull ResponseEntity<@NonNull WorkspaceAdministrativeAuditListResponse> workspaceAudit(
      @PathVariable @NonNull UUID workspaceId
  ) {
    return ResponseEntity.ok(WorkspaceAdministrationRestMapper.toAuditListResponse(
        listWorkspaceAuditUseCase.list(WorkspaceId.of(workspaceId))
    ));
  }
}
