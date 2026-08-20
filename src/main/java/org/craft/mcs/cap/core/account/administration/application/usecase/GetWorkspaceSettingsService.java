package org.craft.mcs.cap.core.account.administration.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.context.application.usecase.WorkspacePermissionChecker;
import org.craft.mcs.cap.core.account.workspace.application.WorkspaceSettingsNotFoundException;
import org.craft.mcs.cap.core.account.workspace.application.WorkspaceSettingsViewMapper;
import org.craft.mcs.cap.core.account.workspace.application.contract.WorkspaceSettingsView;
import org.craft.mcs.cap.core.account.workspace.application.port.in.GetWorkspaceSettingsUseCase;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.WORKSPACE_SETTINGS_READ;


/** Returns auditable workspace settings after checking the current membership scope. */
@Component
@RequiredArgsConstructor
public final class GetWorkspaceSettingsService implements GetWorkspaceSettingsUseCase {

  private final @NonNull WorkspacePermissionChecker workspacePermissionChecker;
  private final @NonNull WorkspaceSettingsRepository workspaceSettingsRepository;

  @Override
  public @NonNull WorkspaceSettingsView get(@NonNull WorkspaceId workspaceId) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    workspacePermissionChecker.requirePermission(workspaceId, WORKSPACE_SETTINGS_READ);
    return workspaceSettingsRepository.findByWorkspaceId(workspaceId)
        .map(WorkspaceSettingsViewMapper::toView)
        .orElseThrow(() -> new WorkspaceSettingsNotFoundException(workspaceId));
  }
}
