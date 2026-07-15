package org.craft.mcs.cap.core.account.invitation.application.usecase;

import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.invitation.application.WorkspaceInvitationNotFoundException;
import org.craft.mcs.cap.core.account.invitation.application.port.in.GetIncomingWorkspaceInvitationUseCase;
import org.craft.mcs.cap.core.account.invitation.application.port.in.IncomingWorkspaceInvitationView;
import org.craft.mcs.cap.core.account.invitation.application.port.in.ListIncomingWorkspaceInvitationsUseCase;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationInviterView;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationRepository;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.invitation.domain.model.value.WorkspaceInvitationId;
import org.craft.mcs.cap.core.account.membership.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.craft.mcs.cap.core.account.workspace.application.WorkspaceNotFoundException;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceView;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/** Builds privacy-safe invitation views for the authenticated recipient. */
@Component
@RequiredArgsConstructor
public final class IncomingWorkspaceInvitationQueryService
    implements GetIncomingWorkspaceInvitationUseCase, ListIncomingWorkspaceInvitationsUseCase {

  private final @NonNull CurrentAccountUserResolver currentAccountUserResolver;
  private final @NonNull WorkspaceInvitationRepository invitationRepository;
  private final @NonNull WorkspaceRepository workspaceRepository;
  private final @NonNull AccountUserRepository accountUserRepository;
  private final @NonNull Clock clock;

  @Override
  public @NonNull IncomingWorkspaceInvitationView get(
      @NonNull WorkspaceInvitationId invitationId
  ) {
    requireNonNull(invitationId, "workspace invitation id cannot be null");
    final var currentUser = currentAccountUserResolver.resolve();
    final var invitation = findInvitation(invitationId);
    if (!invitation.isAddressedTo(currentUser.email())) {
      throw new AccountAccessDeniedException("workspace invitation belongs to another account");
    }

    return toView(invitation);
  }

  @Override
  public @NonNull List<@NonNull IncomingWorkspaceInvitationView> list() {
    final var currentUser = currentAccountUserResolver.resolve();
    return invitationRepository.findAllByInvitedEmail(currentUser.email()).stream()
        .map(this::toView)
        .toList();
  }

  private @NonNull IncomingWorkspaceInvitationView toView(
      @NonNull WorkspaceInvitation invitation
  ) {
    final var workspace = workspaceRepository.findById(invitation.workspaceId())
        .orElseThrow(() -> new WorkspaceNotFoundException(invitation.workspaceId()));
    final var inviter = accountUserRepository.findById(invitation.invitedByUserId())
        .orElseThrow(() -> new WorkspaceInvitationNotFoundException(invitation.invitationId()));

    return new IncomingWorkspaceInvitationView(
        invitation.invitationId(),
        WorkspaceView.from(workspace),
        invitation.role(),
        invitation.effectiveStatus(clock.instant()),
        WorkspaceInvitationInviterView.from(inviter),
        invitation.expiresAt()
    );
  }

  private @NonNull WorkspaceInvitation findInvitation(
      @NonNull WorkspaceInvitationId invitationId
  ) {
    return invitationRepository.findById(invitationId)
        .orElseThrow(() -> new WorkspaceInvitationNotFoundException(invitationId));
  }
}
