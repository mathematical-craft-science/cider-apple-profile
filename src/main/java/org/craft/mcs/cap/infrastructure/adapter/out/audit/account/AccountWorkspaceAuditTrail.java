package org.craft.mcs.cap.infrastructure.adapter.out.audit.account;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.audit.application.port.in.WorkspaceAdministrativeAuditRecorder;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeSubjectId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceAuditTrail;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceSettings;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.SETTINGS_UPDATED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.WORKSPACE_ARCHIVED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.WORKSPACE_CREATED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.WORKSPACE_UPDATED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeSubjectType.WORKSPACE;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeSubjectType.WORKSPACE_SETTINGS;


/** Maps workspace semantic audit operations to the account audit context. */
@Component
@RequiredArgsConstructor
public final class AccountWorkspaceAuditTrail implements WorkspaceAuditTrail {

  private final @NonNull WorkspaceAdministrativeAuditRecorder auditRecorder;

  @Override
  public void recordCreated(
      @NonNull AccountUserId actorUserId,
      @NonNull Workspace workspace
  ) {
    auditRecorder.append(
        workspace.workspaceId(),
        actorUserId,
        WORKSPACE_CREATED,
        WORKSPACE,
        WorkspaceAdministrativeSubjectId.of(workspace.workspaceId().value()),
        null,
        workspaceSnapshot(workspace)
    );
  }

  @Override
  public void recordUpdated(
      @NonNull AccountUserId actorUserId,
      @NonNull Workspace previous,
      @NonNull Workspace updated
  ) {
    recordWorkspaceChange(actorUserId, previous, updated, WORKSPACE_UPDATED);
  }

  @Override
  public void recordArchived(
      @NonNull AccountUserId actorUserId,
      @NonNull Workspace previous,
      @NonNull Workspace archived
  ) {
    recordWorkspaceChange(actorUserId, previous, archived, WORKSPACE_ARCHIVED);
  }

  @Override
  public void recordSettingsUpdated(
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceSettings previous,
      @NonNull WorkspaceSettings updated
  ) {
    auditRecorder.append(
        updated.workspaceId(),
        actorUserId,
        SETTINGS_UPDATED,
        WORKSPACE_SETTINGS,
        WorkspaceAdministrativeSubjectId.of(updated.workspaceId().value()),
        settingsSnapshot(previous),
        settingsSnapshot(updated)
    );
  }

  private void recordWorkspaceChange(
      @NonNull AccountUserId actorUserId,
      @NonNull Workspace previous,
      @NonNull Workspace updated,
      @NonNull WorkspaceAdministrativeAction action
  ) {
    auditRecorder.append(
        updated.workspaceId(),
        actorUserId,
        action,
        WORKSPACE,
        WorkspaceAdministrativeSubjectId.of(updated.workspaceId().value()),
        workspaceSnapshot(previous),
        workspaceSnapshot(updated)
    );
  }

  private static @NonNull WorkspaceAdministrativeAuditValue workspaceSnapshot(
      @NonNull Workspace workspace
  ) {
    return WorkspaceAdministrativeAuditValue.of("name=" + workspace.name().value()
        + ";type=" + workspace.type()
        + ";status=" + workspace.status());
  }

  private static @NonNull WorkspaceAdministrativeAuditValue settingsSnapshot(
      @NonNull WorkspaceSettings settings
  ) {
    return WorkspaceAdministrativeAuditValue.of(
        "measurementStaleAfter=" + settings.measurementStaleAfter()
            + ";defaultAcidityBasis=" + settings.defaultAcidityBasis()
    );
  }
}
