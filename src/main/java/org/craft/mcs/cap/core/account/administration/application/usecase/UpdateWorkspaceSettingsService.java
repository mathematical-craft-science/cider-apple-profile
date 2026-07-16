package org.craft.mcs.cap.core.account.administration.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.context.application.usecase.WorkspacePermissionChecker;
import org.craft.mcs.cap.core.account.shared.application.port.out.WorkspaceAdministrationLock;
import org.craft.mcs.cap.core.account.workspace.application.WorkspaceSettingsNotFoundException;
import org.craft.mcs.cap.core.account.workspace.application.model.WorkspaceSettingsView;
import org.craft.mcs.cap.core.account.workspace.application.port.in.UpdateWorkspaceSettingsCommand;
import org.craft.mcs.cap.core.account.workspace.application.port.in.UpdateWorkspaceSettingsUseCase;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceAuditTrail;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.WORKSPACE_SETTINGS_UPDATE;
import static org.craft.mcs.cap.core.account.workspace.application.WorkspaceSettingsViewMapper.toView;


/**
 * Applies a partial workspace-settings update without hiding concurrent modifications.
 */
@Component
@RequiredArgsConstructor
public final class UpdateWorkspaceSettingsService
    implements UpdateWorkspaceSettingsUseCase {

  private final @NonNull WorkspacePermissionChecker workspacePermissionChecker;
  private final @NonNull WorkspaceSettingsRepository workspaceSettingsRepository;
  private final @NonNull WorkspaceAdministrationLock workspaceAdministrationLock;
  private final @NonNull WorkspaceAuditTrail auditTrail;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull WorkspaceSettingsView update(@NonNull UpdateWorkspaceSettingsCommand command) {
    requireNonNull(command, "update workspace settings command cannot be null");

    return transactionHelper.callInTransactionNonNull(() -> {
      workspaceAdministrationLock.acquire(command.workspaceId());
      final var context = workspacePermissionChecker.requirePermission(
          command.workspaceId(),
          WORKSPACE_SETTINGS_UPDATE
      );
      final var state = workspaceSettingsRepository.findByWorkspaceId(command.workspaceId())
          .orElseThrow(() -> new WorkspaceSettingsNotFoundException(command.workspaceId()));
      final var settings = state.settings().update(
          requireNonNullElse(command.measurementStaleAfter(), state.settings().measurementStaleAfter()),
          requireNonNullElse(command.defaultAcidityBasis(), state.settings().defaultAcidityBasis())
      );
      final var updated = workspaceSettingsRepository.update(settings, command.expectedRevision())
          .orElseThrow(() -> new WorkspaceSettingsNotFoundException(command.workspaceId()));
      auditTrail.recordSettingsUpdated(context.user().userId(), state.settings(), updated.settings());
      return toView(updated);
    });
  }
}
