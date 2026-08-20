package org.craft.mcs.cap.core.account.invitation.application.usecase;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.context.application.usecase.WorkspacePermissionChecker;
import org.craft.mcs.cap.core.account.invitation.application.WorkspaceInvitationConflictException;
import org.craft.mcs.cap.core.account.invitation.application.WorkspaceInvitationNotFoundException;
import org.craft.mcs.cap.core.account.invitation.application.WorkspaceInvitationRecipientMismatchException;
import org.craft.mcs.cap.core.account.invitation.application.contract.WorkspaceInvitationDeliveryRequest;
import org.craft.mcs.cap.core.account.invitation.application.contract.WorkspaceInvitationDefaults;
import org.craft.mcs.cap.core.account.invitation.application.port.in.InviteWorkspaceMemberCommand;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationCommand;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationUseCase;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationView;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationAuditTrail;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationDeliveryScheduler;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationRepository;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.shared.application.port.out.WorkspaceAdministrationLock;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.craft.mcs.cap.core.account.workspace.application.WorkspaceNotFoundException;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
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
  private final @NonNull WorkspaceInvitationAuditTrail auditTrail;
  private final @NonNull WorkspaceInvitationDeliveryScheduler deliveryScheduler;
  private final @NonNull WorkspaceInvitationDefaults invitationDefaults;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull Clock clock;

  private static void requireRecipient(
      @NonNull WorkspaceInvitation invitation,
      @NonNull EmailAddress email
  ) {
    if (!invitation.isAddressedTo(email)) {
      throw new WorkspaceInvitationRecipientMismatchException();
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
      expireElapsedInvitationOrRejectPending(command);
      requireNotAlreadyMember(command);
      final var invitation = invitationRepository.create(WorkspaceInvitation.create(
          command.workspaceId(),
          command.invitedEmail(),
          command.role(),
          context.user().userId(),
          clock.instant(),
          invitationDefaults.timeToLive()
      ));
      auditTrail.recordCreated(context.user().userId(), invitation);
      deliveryScheduler.schedule(WorkspaceInvitationDeliveryRequest.from(invitation));
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
      final var invitationState = findLockedInvitation(invitationId);
      final var invitation = invitationState.aggregate();
      requireRecipient(invitation, user.email());
      final var workspace = findActiveWorkspace(invitation.workspaceId());
      final var existingMembership = membershipRepository.findVersionedByWorkspaceIdAndUserId(
          invitation.workspaceId(),
          user.userId()
      );
      existingMembership.map(VersionedAggregate::aggregate)
          .filter(membership -> !membership.isRemoved())
          .ifPresent(_ -> {
            throw new WorkspaceInvitationConflictException("account is already a workspace member");
          });
      final var accepted = invitationRepository.update(
          invitationState.withAggregate(invitation.accept(user.userId(), clock.instant()))
      ).aggregate();
      final var membership = existingMembership
          .map(state -> membershipRepository.update(
              state.withAggregate(state.aggregate().rejoin(invitation.role()))
          ).aggregate())
          .orElseGet(() -> membershipRepository.create(WorkspaceMembership.create(
              invitation.workspaceId(),
              user.userId(),
              invitation.role()
          )));
      auditTrail.recordAccepted(user.userId(), accepted);
      return WorkspaceMembershipView.from(membership, workspace);
    });
  }

  @Override
  public @NonNull WorkspaceInvitationView reject(@NonNull WorkspaceInvitationId invitationId) {
    requireNonNull(invitationId, "workspace invitation id cannot be null");
    return transactionHelper.callInTransactionNonNull(() -> {
      final var user = currentAccountUserResolver.resolve();
      final var invitationState = findLockedInvitation(invitationId);
      final var invitation = invitationState.aggregate();
      requireRecipient(invitation, user.email());
      final var rejected = invitationRepository.update(
          invitationState.withAggregate(invitation.reject(clock.instant()))
      ).aggregate();
      auditTrail.recordRejected(user.userId(), rejected);
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
      final var invitationState = findInvitation(command);
      final var invitation = invitationState.aggregate();
      final var revoked = invitationRepository.update(
          invitationState.withAggregate(invitation.revoke(clock.instant()))
      ).aggregate();
      auditTrail.recordRevoked(context.user().userId(), revoked);
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
      final var invitationState = findInvitation(command);
      final var invitation = invitationState.aggregate();
      final var previousStatus = invitation.effectiveStatus(clock.instant());
      final var resent = invitationRepository.update(invitationState.withAggregate(
          invitation.resend(clock.instant(), invitationDefaults.timeToLive())
      )).aggregate();
      auditTrail.recordResent(context.user().userId(), previousStatus, resent);
      deliveryScheduler.schedule(WorkspaceInvitationDeliveryRequest.from(resent));
      return toView(resent);
    });
  }

  private void expireElapsedInvitationOrRejectPending(@NonNull InviteWorkspaceMemberCommand command) {
    final var pendingState = invitationRepository.findVersionedByWorkspaceIdAndInvitedEmailAndStatus(
        command.workspaceId(),
        command.invitedEmail(),
        PENDING
    );
    if (pendingState.isEmpty()) {
      return;
    }

    final var state = pendingState.orElseThrow();
    final var invitation = state.aggregate();
    final var now = clock.instant();
    if (invitation.effectiveStatus(now) == PENDING) {
      throw new WorkspaceInvitationConflictException("pending invitation already exists for this email");
    }
    invitationRepository.update(state.withAggregate(invitation.expire(now)));
  }

  private void requireNotAlreadyMember(@NonNull InviteWorkspaceMemberCommand command) {
    accountUserRepository.findAllByEmail(command.invitedEmail()).stream()
        .map(user -> membershipRepository.findVersionedByWorkspaceIdAndUserId(
            command.workspaceId(),
            user.userId()
        ))
        .flatMap(Optional::stream)
        .map(VersionedAggregate::aggregate)
        .filter(membership -> !membership.isRemoved())
        .findAny()
        .ifPresent(_ -> {
          throw new WorkspaceInvitationConflictException("account is already a workspace member");
        });
  }

  private @NonNull VersionedAggregate<WorkspaceInvitation> findInvitation(
      @NonNull WorkspaceInvitationId invitationId
  ) {
    return invitationRepository.findVersionedById(invitationId)
        .orElseThrow(() -> new WorkspaceInvitationNotFoundException(invitationId));
  }

  private @NonNull VersionedAggregate<WorkspaceInvitation> findLockedInvitation(
      @NonNull WorkspaceInvitationId invitationId
  ) {
    final var workspaceId = findInvitation(invitationId).aggregate().workspaceId();
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

  private @NonNull VersionedAggregate<WorkspaceInvitation> findInvitation(
      @NonNull WorkspaceInvitationCommand command
  ) {
    final var state = findInvitation(command.invitationId());
    final var invitation = state.aggregate();
    if (!invitation.workspaceId().equals(command.workspaceId())) {
      throw new WorkspaceInvitationNotFoundException(command.invitationId());
    }
    return state;
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

}
