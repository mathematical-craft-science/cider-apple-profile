package org.craft.mcs.cap.core.account.administration.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.context.application.usecase.WorkspacePermissionChecker;
import org.craft.mcs.cap.core.account.shared.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.membership.application.WorkspaceMembershipNotFoundException;
import org.craft.mcs.cap.core.account.administration.application.port.in.ChangeWorkspaceMemberRoleCommand;
import org.craft.mcs.cap.core.account.administration.application.port.in.TransferWorkspaceOwnershipCommand;
import org.craft.mcs.cap.core.account.administration.application.port.in.WorkspaceMemberCommand;
import org.craft.mcs.cap.core.account.administration.application.port.in.WorkspaceMemberView;
import org.craft.mcs.cap.core.account.administration.application.port.in.WorkspaceMembershipManagementUseCase;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipAuditTrail;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceOwnershipPolicy;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.core.account.user.application.AccountNotFoundException;
import org.craft.mcs.cap.core.account.user.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.shared.application.port.out.WorkspaceAdministrationLock;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.WORKSPACE_MEMBERS_MANAGE;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.WORKSPACE_OWNERSHIP_TRANSFER;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.WORKSPACE_READ;


/**
 * Serializes member administration and enforces active-owner invariants across memberships.
 */
@Component
@RequiredArgsConstructor
public final class WorkspaceMembershipManagementService
    implements WorkspaceMembershipManagementUseCase {

  private static final @NonNull WorkspaceOwnershipPolicy OWNERSHIP_POLICY =
      new WorkspaceOwnershipPolicy();

  private final @NonNull WorkspacePermissionChecker workspacePermissionChecker;
  private final @NonNull AccountUserRepository accountUserRepository;
  private final @NonNull WorkspaceMembershipRepository membershipRepository;
  private final @NonNull WorkspaceAdministrationLock workspaceAdministrationLock;
  private final @NonNull WorkspaceMembershipAuditTrail auditTrail;
  private final @NonNull TransactionHelper transactionHelper;

  private static void requireOwnerManagedByOwner(
      @NonNull AccountRole actorRole,
      @NonNull WorkspaceMembership target
  ) {
    if (target.role() == AccountRole.OWNER && actorRole != AccountRole.OWNER) {
      throw new AccountAccessDeniedException("only an owner can administer another owner membership");
    }
  }

  @Override
  public @NonNull WorkspaceMemberView changeRole(@NonNull ChangeWorkspaceMemberRoleCommand command) {
    requireNonNull(command, "change workspace member role command cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      workspaceAdministrationLock.acquire(command.workspaceId());
      final var context = workspacePermissionChecker.requirePermission(
          command.workspaceId(),
          WORKSPACE_MEMBERS_MANAGE
      );
      final var targetState = findMembership(command.workspaceId(), command.membershipId());
      final var target = targetState.aggregate();
      requireOwnerManagedByOwner(context.activeMembership().role(), target);
      final var memberships = membershipRepository.findAllByWorkspaceId(command.workspaceId());
      if (target.isActiveOwner()) {
        OWNERSHIP_POLICY.requireCanDeactivate(target, memberships);
      }
      final var updated = membershipRepository.update(
          targetState.withAggregate(target.changeAssignableRole(command.role()))
      ).aggregate();
      auditTrail.recordRoleChanged(context.user().userId(), target, updated);
      return toView(updated);
    });
  }

  @Override
  public @NonNull WorkspaceMemberView suspend(@NonNull WorkspaceMemberCommand command) {
    return changeStatus(command, true);
  }

  @Override
  public @NonNull WorkspaceMemberView reactivate(@NonNull WorkspaceMemberCommand command) {
    return changeStatus(command, false);
  }

  @Override
  public void remove(@NonNull WorkspaceMemberCommand command) {
    requireNonNull(command, "workspace member command cannot be null");
    transactionHelper.executeInTransaction(() -> {
      workspaceAdministrationLock.acquire(command.workspaceId());
      final var context = workspacePermissionChecker.requirePermission(
          command.workspaceId(),
          WORKSPACE_MEMBERS_MANAGE
      );
      final var targetState = findMembership(command.workspaceId(), command.membershipId());
      final var target = targetState.aggregate();
      requireOwnerManagedByOwner(context.activeMembership().role(), target);
      OWNERSHIP_POLICY.requireCanDeactivate(
          target,
          membershipRepository.findAllByWorkspaceId(command.workspaceId())
      );
      membershipRepository.delete(targetState);
      auditTrail.recordRemoved(context.user().userId(), target);
    });
  }

  @Override
  public void leave(@NonNull WorkspaceId workspaceId) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    transactionHelper.executeInTransaction(() -> {
      workspaceAdministrationLock.acquire(workspaceId);
      final var context = workspacePermissionChecker.requirePermission(workspaceId, WORKSPACE_READ);
      final var membershipState = findMembership(
          workspaceId,
          context.activeMembership().membershipId()
      );
      final var membership = membershipState.aggregate();
      OWNERSHIP_POLICY.requireCanDeactivate(
          membership,
          membershipRepository.findAllByWorkspaceId(workspaceId)
      );
      membershipRepository.delete(membershipState);
      auditTrail.recordLeft(context.user().userId(), membership);
    });
  }

  @Override
  public @NonNull WorkspaceMemberView transferOwnership(
      @NonNull TransferWorkspaceOwnershipCommand command
  ) {
    requireNonNull(command, "transfer workspace ownership command cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      workspaceAdministrationLock.acquire(command.workspaceId());
      final var context = workspacePermissionChecker.requirePermission(
          command.workspaceId(),
          WORKSPACE_OWNERSHIP_TRANSFER
      );
      final var currentOwnerState = findMembership(
          command.workspaceId(),
          context.activeMembership().membershipId()
      );
      final var targetState = findMembership(command.workspaceId(), command.targetMembershipId());
      final var currentOwner = currentOwnerState.aggregate();
      final var target = targetState.aggregate();
      final var transfer = OWNERSHIP_POLICY.transfer(currentOwner, target);
      final var newOwner = membershipRepository.update(
          targetState.withAggregate(transfer.newOwner())
      ).aggregate();
      membershipRepository.update(currentOwnerState.withAggregate(transfer.previousOwner()));
      auditTrail.recordOwnershipTransferred(context.user().userId(), currentOwner, newOwner);
      return toView(newOwner);
    });
  }

  private @NonNull WorkspaceMemberView changeStatus(
      @NonNull WorkspaceMemberCommand command,
      boolean suspend
  ) {
    requireNonNull(command, "workspace member command cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      workspaceAdministrationLock.acquire(command.workspaceId());
      final var context = workspacePermissionChecker.requirePermission(
          command.workspaceId(),
          WORKSPACE_MEMBERS_MANAGE
      );
      final var targetState = findMembership(command.workspaceId(), command.membershipId());
      final var target = targetState.aggregate();
      requireOwnerManagedByOwner(context.activeMembership().role(), target);
      if (suspend) {
        OWNERSHIP_POLICY.requireCanDeactivate(
            target,
            membershipRepository.findAllByWorkspaceId(command.workspaceId())
        );
      }
      final var updatedMembership = suspend ? target.suspend() : target.reactivate();
      final var updated = membershipRepository.update(
          targetState.withAggregate(updatedMembership)
      ).aggregate();
      if (suspend) {
        auditTrail.recordSuspended(context.user().userId(), target, updated);
      } else {
        auditTrail.recordReactivated(context.user().userId(), target, updated);
      }
      return toView(updated);
    });
  }

  private @NonNull VersionedAggregate<WorkspaceMembership> findMembership(
      @NonNull WorkspaceId workspaceId,
      @NonNull WorkspaceMembershipId membershipId
  ) {
    final var state = membershipRepository.findVersionedById(membershipId)
        .orElseThrow(() -> new WorkspaceMembershipNotFoundException(membershipId));
    final var membership = state.aggregate();
    if (!membership.workspaceId().equals(workspaceId)) {
      throw new WorkspaceMembershipNotFoundException(membershipId);
    }
    return state;
  }

  private @NonNull WorkspaceMemberView toView(@NonNull WorkspaceMembership membership) {
    final var user = accountUserRepository.findById(membership.userId())
        .orElseThrow(AccountNotFoundException::new);
    return new WorkspaceMemberView(
        membership.membershipId(),
        AccountUserView.from(user),
        membership.role(),
        membership.status()
    );
  }

}
