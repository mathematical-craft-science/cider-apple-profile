package org.craft.mcs.cap.core.account.membership.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.audit.application.usecase.WorkspaceAdministrativeAuditAppender;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeSubjectId;
import org.craft.mcs.cap.core.account.membership.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.membership.application.WorkspaceMembershipNotFoundException;
import org.craft.mcs.cap.core.account.membership.application.port.in.ChangeWorkspaceMemberRoleCommand;
import org.craft.mcs.cap.core.account.membership.application.port.in.TransferWorkspaceOwnershipCommand;
import org.craft.mcs.cap.core.account.membership.application.port.in.WorkspaceMemberCommand;
import org.craft.mcs.cap.core.account.membership.application.port.in.WorkspaceMemberView;
import org.craft.mcs.cap.core.account.membership.application.port.in.WorkspaceMembershipManagementUseCase;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceOwnershipPolicy;
import org.craft.mcs.cap.core.account.membership.domain.model.value.WorkspaceMembershipId;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.user.application.AccountNotFoundException;
import org.craft.mcs.cap.core.account.user.application.port.in.AccountUserView;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceAdministrationLock;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.MEMBERSHIP_LEFT;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.MEMBERSHIP_REACTIVATED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.MEMBERSHIP_REMOVED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.MEMBERSHIP_ROLE_CHANGED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.MEMBERSHIP_SUSPENDED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.OWNERSHIP_TRANSFERRED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeSubjectType.MEMBERSHIP;
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
  private final @NonNull WorkspaceAdministrativeAuditAppender auditAppender;
  private final @NonNull TransactionHelper transactionHelper;

  private static void requireOwnerManagedByOwner(
      @NonNull AccountRole actorRole,
      @NonNull WorkspaceMembership target
  ) {
    if (target.role() == AccountRole.OWNER && actorRole != AccountRole.OWNER) {
      throw new AccountAccessDeniedException("only an owner can administer another owner membership");
    }
  }

  private static @NonNull WorkspaceAdministrativeAuditValue membershipSnapshot(
      @NonNull WorkspaceMembership membership
  ) {
    return WorkspaceAdministrativeAuditValue.of("userId=" + membership.userId().value()
        + ";role=" + membership.role()
        + ";status=" + membership.status());
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
      final var target = findMembership(command.workspaceId(), command.membershipId());
      requireOwnerManagedByOwner(context.activeMembership().role(), target);
      final var memberships = membershipRepository.findAllByWorkspaceId(command.workspaceId());
      if (target.isActiveOwner()) {
        OWNERSHIP_POLICY.requireCanDeactivate(target, memberships);
      }
      final var updated = membershipRepository.save(target.changeAssignableRole(command.role()));
      appendAudit(
          command.workspaceId(),
          context.user().userId(),
          updated,
          MEMBERSHIP_ROLE_CHANGED,
          WorkspaceAdministrativeAuditValue.of(target.role().name()),
          WorkspaceAdministrativeAuditValue.of(updated.role().name())
      );
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
      final var target = findMembership(command.workspaceId(), command.membershipId());
      requireOwnerManagedByOwner(context.activeMembership().role(), target);
      OWNERSHIP_POLICY.requireCanDeactivate(
          target,
          membershipRepository.findAllByWorkspaceId(command.workspaceId())
      );
      membershipRepository.deleteById(target.membershipId());
      appendAudit(
          command.workspaceId(),
          context.user().userId(),
          target,
          MEMBERSHIP_REMOVED,
          membershipSnapshot(target),
          null
      );
    });
  }

  @Override
  public void leave(@NonNull WorkspaceId workspaceId) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    transactionHelper.executeInTransaction(() -> {
      workspaceAdministrationLock.acquire(workspaceId);
      final var context = workspacePermissionChecker.requirePermission(workspaceId, WORKSPACE_READ);
      final var membership = findMembership(
          workspaceId,
          context.activeMembership().membershipId()
      );
      OWNERSHIP_POLICY.requireCanDeactivate(
          membership,
          membershipRepository.findAllByWorkspaceId(workspaceId)
      );
      membershipRepository.deleteById(membership.membershipId());
      appendAudit(
          workspaceId,
          context.user().userId(),
          membership,
          MEMBERSHIP_LEFT,
          membershipSnapshot(membership),
          null
      );
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
      final var currentOwner = findMembership(
          command.workspaceId(),
          context.activeMembership().membershipId()
      );
      final var target = findMembership(command.workspaceId(), command.targetMembershipId());
      final var transfer = OWNERSHIP_POLICY.transfer(currentOwner, target);
      final var newOwner = membershipRepository.save(transfer.newOwner());
      membershipRepository.save(transfer.previousOwner());
      appendAudit(
          command.workspaceId(),
          context.user().userId(),
          newOwner,
          OWNERSHIP_TRANSFERRED,
          WorkspaceAdministrativeAuditValue.of(currentOwner.membershipId().value().toString()),
          WorkspaceAdministrativeAuditValue.of(newOwner.membershipId().value().toString())
      );
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
      final var target = findMembership(command.workspaceId(), command.membershipId());
      requireOwnerManagedByOwner(context.activeMembership().role(), target);
      if (suspend) {
        OWNERSHIP_POLICY.requireCanDeactivate(
            target,
            membershipRepository.findAllByWorkspaceId(command.workspaceId())
        );
      }
      final var updated = membershipRepository.save(suspend ? target.suspend() : target.reactivate());
      appendAudit(
          command.workspaceId(),
          context.user().userId(),
          updated,
          suspend ? MEMBERSHIP_SUSPENDED : MEMBERSHIP_REACTIVATED,
          WorkspaceAdministrativeAuditValue.of(target.status().name()),
          WorkspaceAdministrativeAuditValue.of(updated.status().name())
      );
      return toView(updated);
    });
  }

  private @NonNull WorkspaceMembership findMembership(
      @NonNull WorkspaceId workspaceId,
      @NonNull WorkspaceMembershipId membershipId
  ) {
    final var membership = membershipRepository.findById(membershipId)
        .orElseThrow(() -> new WorkspaceMembershipNotFoundException(membershipId));
    if (!membership.workspaceId().equals(workspaceId)) {
      throw new WorkspaceMembershipNotFoundException(membershipId);
    }
    return membership;
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

  private void appendAudit(
      @NonNull WorkspaceId workspaceId,
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceMembership membership,
      @NonNull WorkspaceAdministrativeAction action,
      @Nullable WorkspaceAdministrativeAuditValue previousValue,
      @Nullable WorkspaceAdministrativeAuditValue newValue
  ) {
    auditAppender.append(
        workspaceId,
        actorUserId,
        action,
        MEMBERSHIP,
        WorkspaceAdministrativeSubjectId.forMembership(membership.membershipId()),
        previousValue,
        newValue
    );
  }
}
