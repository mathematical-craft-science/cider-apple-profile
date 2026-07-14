package org.craft.mcs.cap.core.account.workspace.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.audit.application.usecase.WorkspaceAdministrativeAuditAppender;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeSubjectId;
import org.craft.mcs.cap.core.account.membership.application.usecase.WorkspacePermissionChecker;
import org.craft.mcs.cap.core.account.workspace.application.WorkspaceSettingsNotFoundException;
import org.craft.mcs.cap.core.account.workspace.application.port.in.UpdateWorkspaceSettingsCommand;
import org.craft.mcs.cap.core.account.workspace.application.port.in.UpdateWorkspaceSettingsUseCase;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceSettingsView;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceAdministrationLock;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.SETTINGS_UPDATED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeSubjectType.WORKSPACE_SETTINGS;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.WORKSPACE_SETTINGS_UPDATE;


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
  private final @NonNull WorkspaceAdministrativeAuditAppender auditAppender;
  private final @NonNull TransactionHelper transactionHelper;

  private static @NonNull WorkspaceAdministrativeAuditValue settingsSnapshot(
      @NonNull WorkspaceSettings settings
  ) {
    return WorkspaceAdministrativeAuditValue.of(
        "measurementStaleAfter=" + settings.measurementStaleAfter()
            + ";defaultAcidityBasis=" + settings.defaultAcidityBasis()
    );
  }

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
      auditAppender.append(
          command.workspaceId(),
          context.user().userId(),
          SETTINGS_UPDATED,
          WORKSPACE_SETTINGS,
          WorkspaceAdministrativeSubjectId.forWorkspace(command.workspaceId()),
          settingsSnapshot(state.settings()),
          settingsSnapshot(updated.settings())
      );
      return WorkspaceSettingsView.from(updated);
    });
  }
}
