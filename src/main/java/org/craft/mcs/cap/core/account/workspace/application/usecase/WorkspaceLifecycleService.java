package org.craft.mcs.cap.core.account.workspace.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.audit.application.usecase.WorkspaceAdministrativeAuditAppender;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeSubjectId;
import org.craft.mcs.cap.core.account.membership.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.membership.application.usecase.WorkspacePermissionChecker;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.craft.mcs.cap.core.account.workspace.application.WorkspaceNotFoundException;
import org.craft.mcs.cap.core.account.workspace.application.model.WorkspaceSettingsDefaults;
import org.craft.mcs.cap.core.account.workspace.application.port.in.CreateWorkspaceCommand;
import org.craft.mcs.cap.core.account.workspace.application.port.in.UpdateWorkspaceCommand;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceLifecycleUseCase;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceView;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceAdministrationLock;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.WORKSPACE_ARCHIVED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.WORKSPACE_CREATED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.WORKSPACE_UPDATED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeSubjectType.WORKSPACE;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.WORKSPACE_ARCHIVE;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.WORKSPACE_UPDATE;


/**
 * Executes workspace lifecycle commands without coupling workspace creation to IAM organizations.
 */
@Component
@RequiredArgsConstructor
public final class WorkspaceLifecycleService
    implements WorkspaceLifecycleUseCase {

  private final @NonNull CurrentAccountUserResolver currentAccountUserResolver;
  private final @NonNull WorkspacePermissionChecker workspacePermissionChecker;
  private final @NonNull WorkspaceRepository workspaceRepository;
  private final @NonNull WorkspaceSettingsRepository workspaceSettingsRepository;
  private final @NonNull WorkspaceMembershipRepository membershipRepository;
  private final @NonNull WorkspaceAdministrationLock workspaceAdministrationLock;
  private final @NonNull WorkspaceAdministrativeAuditAppender auditAppender;
  private final @NonNull WorkspaceSettingsDefaults workspaceSettingsDefaults;
  private final @NonNull TransactionHelper transactionHelper;

  private static @NonNull WorkspaceAdministrativeAuditValue workspaceSnapshot(
      @NonNull Workspace workspace
  ) {
    return WorkspaceAdministrativeAuditValue.of("name=" + workspace.name().value()
        + ";type=" + workspace.type()
        + ";status=" + workspace.status());
  }

  @Override
  public @NonNull WorkspaceMembershipView create(@NonNull CreateWorkspaceCommand command) {
    requireNonNull(command, "create workspace command cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      final var user = currentAccountUserResolver.resolve();
      final var workspace = workspaceRepository.save(Workspace.create(command.name(), command.type()));
      workspaceSettingsRepository.create(new WorkspaceSettings(
          workspace.workspaceId(),
          workspaceSettingsDefaults.measurementStaleAfter(),
          workspaceSettingsDefaults.defaultAcidityBasis()
      ));
      final var membership = membershipRepository.save(
          WorkspaceMembership.createOwner(workspace.workspaceId(), user.userId())
      );
      auditAppender.append(
          workspace.workspaceId(),
          user.userId(),
          WORKSPACE_CREATED,
          WORKSPACE,
          WorkspaceAdministrativeSubjectId.forWorkspace(workspace.workspaceId()),
          null,
          workspaceSnapshot(workspace)
      );
      return WorkspaceMembershipView.from(membership, workspace);
    });
  }

  @Override
  public @NonNull WorkspaceView update(@NonNull UpdateWorkspaceCommand command) {
    requireNonNull(command, "update workspace command cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      workspaceAdministrationLock.acquire(command.workspaceId());
      final var context = workspacePermissionChecker.requirePermission(
          command.workspaceId(),
          WORKSPACE_UPDATE
      );
      final var current = findWorkspace(command.workspaceId());
      final var updated = workspaceRepository.save(current.update(command.name(), command.type()));
      auditAppender.append(
          command.workspaceId(),
          context.user().userId(),
          WORKSPACE_UPDATED,
          WORKSPACE,
          WorkspaceAdministrativeSubjectId.forWorkspace(command.workspaceId()),
          workspaceSnapshot(current),
          workspaceSnapshot(updated)
      );
      return WorkspaceView.from(updated);
    });
  }

  @Override
  public @NonNull WorkspaceView archive(@NonNull WorkspaceId workspaceId) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      workspaceAdministrationLock.acquire(workspaceId);
      final var context = workspacePermissionChecker.requirePermission(workspaceId, WORKSPACE_ARCHIVE);
      final var current = findWorkspace(workspaceId);
      final var archived = workspaceRepository.save(current.archive());
      auditAppender.append(
          workspaceId,
          context.user().userId(),
          WORKSPACE_ARCHIVED,
          WORKSPACE,
          WorkspaceAdministrativeSubjectId.forWorkspace(workspaceId),
          workspaceSnapshot(current),
          workspaceSnapshot(archived)
      );
      return WorkspaceView.from(archived);
    });
  }

  private @NonNull Workspace findWorkspace(@NonNull WorkspaceId workspaceId) {
    return workspaceRepository.findById(workspaceId)
        .orElseThrow(() -> new WorkspaceNotFoundException(workspaceId));
  }
}
