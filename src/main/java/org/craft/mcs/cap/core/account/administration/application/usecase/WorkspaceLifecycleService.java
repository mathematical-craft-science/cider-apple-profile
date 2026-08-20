package org.craft.mcs.cap.core.account.administration.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.context.application.usecase.WorkspacePermissionChecker;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.craft.mcs.cap.core.account.workspace.application.WorkspaceNotFoundException;
import org.craft.mcs.cap.core.account.workspace.application.contract.WorkspaceSettingsDefaults;
import org.craft.mcs.cap.core.account.administration.application.port.in.CreateWorkspaceCommand;
import org.craft.mcs.cap.core.account.administration.application.port.in.UpdateWorkspaceCommand;
import org.craft.mcs.cap.core.account.administration.application.port.in.WorkspaceLifecycleUseCase;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceView;
import org.craft.mcs.cap.core.account.shared.application.port.out.WorkspaceAdministrationLock;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceAuditTrail;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
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
  private final @NonNull WorkspaceAuditTrail auditTrail;
  private final @NonNull WorkspaceSettingsDefaults workspaceSettingsDefaults;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull WorkspaceMembershipView create(@NonNull CreateWorkspaceCommand command) {
    requireNonNull(command, "create workspace command cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      final var user = currentAccountUserResolver.resolve();
      final var workspace = workspaceRepository.create(Workspace.create(command.name(), command.type()));
      workspaceSettingsRepository.create(new WorkspaceSettings(
          workspace.workspaceId(),
          workspaceSettingsDefaults.measurementStaleAfter(),
          workspaceSettingsDefaults.defaultAcidityBasis()
      ));
      final var membership = membershipRepository.create(
          WorkspaceMembership.createOwner(workspace.workspaceId(), user.userId())
      );
      auditTrail.recordCreated(user.userId(), workspace);
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
      final var state = findWorkspace(command.workspaceId());
      final var current = state.aggregate();
      final var updated = workspaceRepository.update(
          state.withAggregate(current.update(command.name(), command.type()))
      ).aggregate();
      auditTrail.recordUpdated(context.user().userId(), current, updated);
      return WorkspaceView.from(updated);
    });
  }

  @Override
  public @NonNull WorkspaceView archive(@NonNull WorkspaceId workspaceId) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      workspaceAdministrationLock.acquire(workspaceId);
      final var context = workspacePermissionChecker.requirePermission(workspaceId, WORKSPACE_ARCHIVE);
      final var state = findWorkspace(workspaceId);
      final var current = state.aggregate();
      final var archived = workspaceRepository.update(state.withAggregate(current.archive())).aggregate();
      auditTrail.recordArchived(context.user().userId(), current, archived);
      return WorkspaceView.from(archived);
    });
  }

  private @NonNull VersionedAggregate<Workspace> findWorkspace(@NonNull WorkspaceId workspaceId) {
    return workspaceRepository.findVersionedById(workspaceId)
        .orElseThrow(() -> new WorkspaceNotFoundException(workspaceId));
  }
}
