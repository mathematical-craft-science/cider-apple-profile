package org.craft.mcs.cap.core.account.invitation.application.usecase;

import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.audit.application.usecase.WorkspaceAdministrativeAuditAppender;
import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeSubjectId;
import org.craft.mcs.cap.core.account.invitation.application.WorkspaceInvitationConflictException;
import org.craft.mcs.cap.core.account.invitation.application.WorkspaceInvitationNotFoundException;
import org.craft.mcs.cap.core.account.invitation.application.model.WorkspaceInvitationDefaults;
import org.craft.mcs.cap.core.account.invitation.application.port.in.InviteWorkspaceMemberCommand;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationCommand;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationUseCase;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationView;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationRepository;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.core.account.membership.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.membership.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.membership.application.usecase.WorkspacePermissionChecker;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.craft.mcs.cap.core.account.workspace.application.WorkspaceNotFoundException;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceAdministrationLock;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.INVITATION_ACCEPTED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.INVITATION_CREATED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.INVITATION_REJECTED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.INVITATION_RESENT;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.INVITATION_REVOKED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeSubjectType.INVITATION;
import static org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus.PENDING;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.WORKSPACE_MEMBERS_MANAGE;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.WORKSPACE_MEMBERS_READ;


/**
 * Coordinates invitation lifecycle and creates membership only after recipient acceptance.
 */
@Component
@RequiredArgsConstructor
public final class WorkspaceInvitationService
    implements WorkspaceInvitationUseCase {

  private final @NonNull CurrentAccountUserResolver currentAccountUserResolver;
  private final @NonNull WorkspacePermissionChecker workspacePermissionChecker;
  private final @NonNull AccountUserRepository accountUserRepository;
  private final @NonNull WorkspaceRepository workspaceRepository;
  private final @NonNull WorkspaceMembershipRepository membershipRepository;
  private final @NonNull WorkspaceInvitationRepository invitationRepository;
  private final @NonNull WorkspaceAdministrationLock workspaceAdministrationLock;
  private final @NonNull WorkspaceAdministrativeAuditAppender auditAppender;
  private final @NonNull WorkspaceInvitationDefaults invitationDefaults;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull Clock clock;

  private static void requireRecipient(
      @NonNull WorkspaceInvitation invitation,
      @NonNull EmailAddress email
  ) {
    if (!invitation.isAddressedTo(email)) {
      throw new AccountAccessDeniedException("workspace invitation belongs to another account");
    }
  }

  @Override
  public @NonNull WorkspaceInvitationView invite(@NonNull InviteWorkspaceMemberCommand command) {
    requireNonNull(command, "invite workspace member command cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      workspaceAdministrationLock.acquire(command.workspaceId());
      final var context = workspacePermissionChecker.requirePermission(
          command.workspaceId(),
          WORKSPACE_MEMBERS_MANAGE
      );
      requireNoPendingInvitation(command);
      requireNotAlreadyMember(command);
      final var invitation = invitationRepository.save(WorkspaceInvitation.create(
          command.workspaceId(),
          command.invitedEmail(),
          command.role(),
          context.user().userId(),
          clock.instant(),
          invitationDefaults.timeToLive()
      ));
      appendAudit(
          invitation,
          context.user().userId(),
          INVITATION_CREATED,
          null,
          WorkspaceAdministrativeAuditValue.of(invitation.role().name())
      );
      return toView(invitation);
    });
  }

  @Override
  public @NonNull List<@NonNull WorkspaceInvitationView> list(@NonNull WorkspaceId workspaceId) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    workspacePermissionChecker.requirePermission(workspaceId, WORKSPACE_MEMBERS_READ);
    return invitationRepository.findAllByWorkspaceId(workspaceId).stream()
        .map(this::toView)
        .toList();
  }

  @Override
  public @NonNull WorkspaceMembershipView accept(@NonNull WorkspaceInvitationId invitationId) {
    requireNonNull(invitationId, "workspace invitation id cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      final var user = currentAccountUserResolver.resolve();
      final var invitation = findLockedInvitation(invitationId);
      requireRecipient(invitation, user.email());
      final var workspace = findActiveWorkspace(invitation.workspaceId());
      if (membershipRepository.findByWorkspaceIdAndUserId(invitation.workspaceId(), user.userId()).isPresent()) {
        throw new WorkspaceInvitationConflictException("account is already a workspace member");
      }
      final var accepted = invitationRepository.save(invitation.accept(user.userId(), clock.instant()));
      final var membership = membershipRepository.save(WorkspaceMembership.create(
          invitation.workspaceId(),
          user.userId(),
          invitation.role()
      ));
      appendAudit(
          accepted,
          user.userId(),
          INVITATION_ACCEPTED,
          WorkspaceAdministrativeAuditValue.of(PENDING.name()),
          WorkspaceAdministrativeAuditValue.of(accepted.status().name())
      );
      return WorkspaceMembershipView.from(membership, workspace);
    });
  }

  @Override
  public @NonNull WorkspaceInvitationView reject(@NonNull WorkspaceInvitationId invitationId) {
    requireNonNull(invitationId, "workspace invitation id cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      final var user = currentAccountUserResolver.resolve();
      final var invitation = findLockedInvitation(invitationId);
      requireRecipient(invitation, user.email());
      final var rejected = invitationRepository.save(invitation.reject(clock.instant()));
      appendAudit(
          rejected,
          user.userId(),
          INVITATION_REJECTED,
          WorkspaceAdministrativeAuditValue.of(PENDING.name()),
          WorkspaceAdministrativeAuditValue.of(rejected.status().name())
      );
      return toView(rejected);
    });
  }

  @Override
  public @NonNull WorkspaceInvitationView revoke(@NonNull WorkspaceInvitationCommand command) {
    requireNonNull(command, "workspace invitation command cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      workspaceAdministrationLock.acquire(command.workspaceId());
      final var context = workspacePermissionChecker.requirePermission(
          command.workspaceId(),
          WORKSPACE_MEMBERS_MANAGE
      );
      final var invitation = findInvitation(command);
      final var revoked = invitationRepository.save(invitation.revoke(clock.instant()));
      appendAudit(
          revoked,
          context.user().userId(),
          INVITATION_REVOKED,
          WorkspaceAdministrativeAuditValue.of(PENDING.name()),
          WorkspaceAdministrativeAuditValue.of(revoked.status().name())
      );
      return toView(revoked);
    });
  }

  @Override
  public @NonNull WorkspaceInvitationView resend(@NonNull WorkspaceInvitationCommand command) {
    requireNonNull(command, "workspace invitation command cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      workspaceAdministrationLock.acquire(command.workspaceId());
      final var context = workspacePermissionChecker.requirePermission(
          command.workspaceId(),
          WORKSPACE_MEMBERS_MANAGE
      );
      final var invitation = findInvitation(command);
      final var previousStatus = invitation.effectiveStatus(clock.instant());
      final var resent = invitationRepository.save(invitation.resend(
          clock.instant(),
          invitationDefaults.timeToLive()
      ));
      appendAudit(
          resent,
          context.user().userId(),
          INVITATION_RESENT,
          WorkspaceAdministrativeAuditValue.of(previousStatus.name()),
          WorkspaceAdministrativeAuditValue.of(resent.status().name())
      );
      return toView(resent);
    });
  }

  private void requireNoPendingInvitation(@NonNull InviteWorkspaceMemberCommand command) {
    if (invitationRepository.findByWorkspaceIdAndInvitedEmailAndStatus(
        command.workspaceId(),
        command.invitedEmail(),
        PENDING
    ).isPresent()) {
      throw new WorkspaceInvitationConflictException("pending invitation already exists for this email");
    }
  }

  private void requireNotAlreadyMember(@NonNull InviteWorkspaceMemberCommand command) {
    accountUserRepository.findByEmail(command.invitedEmail())
        .flatMap(user -> membershipRepository.findByWorkspaceIdAndUserId(command.workspaceId(), user.userId()))
        .ifPresent(_ -> {
          throw new WorkspaceInvitationConflictException("account is already a workspace member");
        });
  }

  private @NonNull WorkspaceInvitation findInvitation(@NonNull WorkspaceInvitationId invitationId) {
    return invitationRepository.findById(invitationId)
        .orElseThrow(() -> new WorkspaceInvitationNotFoundException(invitationId));
  }

  private @NonNull WorkspaceInvitation findLockedInvitation(
      @NonNull WorkspaceInvitationId invitationId
  ) {
    final var workspaceId = findInvitation(invitationId).workspaceId();
    workspaceAdministrationLock.acquire(workspaceId);
    return findInvitation(invitationId);
  }

  private @NonNull Workspace findActiveWorkspace(
      @NonNull WorkspaceId workspaceId
  ) {
    final var workspace = workspaceRepository.findById(workspaceId)
        .orElseThrow(() -> new WorkspaceNotFoundException(workspaceId));
    if (!workspace.isActive()) {
      throw new WorkspaceInvitationConflictException("workspace invitation belongs to an archived workspace");
    }
    return workspace;
  }

  private @NonNull WorkspaceInvitation findInvitation(@NonNull WorkspaceInvitationCommand command) {
    final var invitation = findInvitation(command.invitationId());
    if (!invitation.workspaceId().equals(command.workspaceId())) {
      throw new WorkspaceInvitationNotFoundException(command.invitationId());
    }
    return invitation;
  }

  private @NonNull WorkspaceInvitationView toView(@NonNull WorkspaceInvitation invitation) {
    return new WorkspaceInvitationView(
        invitation.invitationId(),
        invitation.workspaceId(),
        invitation.invitedEmail(),
        invitation.role(),
        invitation.effectiveStatus(clock.instant()),
        invitation.invitedByUserId(),
        invitation.acceptedByUserId(),
        invitation.expiresAt(),
        invitation.resendCount()
    );
  }

  private void appendAudit(
      @NonNull WorkspaceInvitation invitation,
      @NonNull AccountUserId actorUserId,
      @NonNull WorkspaceAdministrativeAction action,
      @Nullable WorkspaceAdministrativeAuditValue previousValue,
      @Nullable WorkspaceAdministrativeAuditValue newValue
  ) {
    auditAppender.append(
        invitation.workspaceId(),
        actorUserId,
        action,
        INVITATION,
        WorkspaceAdministrativeSubjectId.forInvitation(invitation.invitationId()),
        previousValue,
        newValue
    );
  }
}
