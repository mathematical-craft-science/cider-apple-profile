package org.craft.mcs.cap.core.account.audit.application.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.audit.application.port.in.ListWorkspaceAdministrativeAuditUseCase;
import org.craft.mcs.cap.core.account.audit.application.port.in.WorkspaceAdministrativeAuditView;
import org.craft.mcs.cap.core.account.audit.application.port.out.WorkspaceAdministrativeAuditRepository;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAuditEntry;
import org.craft.mcs.cap.core.account.membership.application.usecase.WorkspacePermissionChecker;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.WORKSPACE_AUDIT_READ;


/**
 * Reads actor-aware workspace administration history after workspace permission verification.
 */
@Component
@RequiredArgsConstructor
public final class WorkspaceAdministrativeAuditService
    implements ListWorkspaceAdministrativeAuditUseCase {

  private final @NonNull WorkspacePermissionChecker workspacePermissionChecker;
  private final @NonNull WorkspaceAdministrativeAuditRepository auditRepository;

  private static @NonNull WorkspaceAdministrativeAuditView toView(
      @NonNull WorkspaceAdministrativeAuditEntry entry
  ) {
    return new WorkspaceAdministrativeAuditView(
        entry.auditEntryId(),
        entry.actorUserId(),
        entry.action(),
        entry.subjectType(),
        entry.subjectId(),
        entry.previousValue(),
        entry.newValue(),
        entry.occurredAt()
    );
  }

  @Override
  public @NonNull List<@NonNull WorkspaceAdministrativeAuditView> list(
      @NonNull WorkspaceId workspaceId
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    workspacePermissionChecker.requirePermission(workspaceId, WORKSPACE_AUDIT_READ);
    return auditRepository.findAllByWorkspaceId(workspaceId).stream()
        .map(WorkspaceAdministrativeAuditService::toView)
        .toList();
  }
}
