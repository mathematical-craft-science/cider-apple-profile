package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.audit;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.audit.application.port.in.ListWorkspaceAdministrativeAuditUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.audit.dto.WorkspaceAdministrativeAuditListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.audit.mapper.WorkspaceAuditRestMapper;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Thin REST adapter for immutable workspace administration history. */
@RestController
@RequiredArgsConstructor
public final class WorkspaceAuditController implements WorkspaceAuditApi {

  private final @NonNull ListWorkspaceAdministrativeAuditUseCase listWorkspaceAuditUseCase;

  @Override
  @GetMapping("/account/workspaces/{workspaceId}/audit")
  public @NonNull ResponseEntity<@NonNull WorkspaceAdministrativeAuditListResponse> workspaceAudit(
      @PathVariable @NonNull UUID workspaceId
  ) {
    return ResponseEntity.ok(WorkspaceAuditRestMapper.toListResponse(
        listWorkspaceAuditUseCase.list(WorkspaceId.of(workspaceId))
    ));
  }
}
