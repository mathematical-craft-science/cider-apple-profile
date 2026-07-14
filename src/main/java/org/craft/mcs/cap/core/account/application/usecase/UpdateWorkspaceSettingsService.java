package org.craft.mcs.cap.core.account.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.application.WorkspaceSettingsNotFoundException;
import org.craft.mcs.cap.core.account.application.port.in.UpdateWorkspaceSettingsCommand;
import org.craft.mcs.cap.core.account.application.port.in.UpdateWorkspaceSettingsUseCase;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceSettingsView;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.craft.mcs.cap.core.account.domain.model.WorkspacePermission.WORKSPACE_SETTINGS_UPDATE;


/** Applies a partial workspace-settings update without hiding concurrent modifications. */
@Component
@RequiredArgsConstructor
public final class UpdateWorkspaceSettingsService implements UpdateWorkspaceSettingsUseCase {

  private final @NonNull WorkspacePermissionChecker workspacePermissionChecker;
  private final @NonNull WorkspaceSettingsRepository workspaceSettingsRepository;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull WorkspaceSettingsView update(@NonNull UpdateWorkspaceSettingsCommand command) {
    requireNonNull(command, "update workspace settings command cannot be null");

    return transactionHelper.callInTransactionNonNull(() -> {
      workspacePermissionChecker.requirePermission(command.workspaceId(), WORKSPACE_SETTINGS_UPDATE);
      final var state = workspaceSettingsRepository.findByWorkspaceId(command.workspaceId())
          .orElseThrow(() -> new WorkspaceSettingsNotFoundException(command.workspaceId()));
      final var settings = state.settings().update(
          requireNonNullElse(command.measurementStaleAfter(), state.settings().measurementStaleAfter()),
          requireNonNullElse(command.defaultAcidityBasis(), state.settings().defaultAcidityBasis())
      );
      return workspaceSettingsRepository.update(settings, command.expectedRevision())
          .map(AccountViewMapper::toView)
          .orElseThrow(() -> new WorkspaceSettingsNotFoundException(command.workspaceId()));
    });
  }
}
