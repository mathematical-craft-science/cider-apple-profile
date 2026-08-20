package org.craft.mcs.cap.core.account.invitation.application.usecase;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.invitation.application.WorkspaceInvitationAuthorNotFoundException;
import org.craft.mcs.cap.core.account.invitation.application.contract.WorkspaceInvitationDeliveryRequest;
import org.craft.mcs.cap.core.account.invitation.application.contract.WorkspaceInvitationEmail;
import org.craft.mcs.cap.core.account.invitation.application.port.in.DeliverWorkspaceInvitationEmailUseCase;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationEmailSender;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationRepository;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.workspace.application.WorkspaceNotFoundException;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus.PENDING;

/**
 * Revalidates an asynchronous invitation delivery request before exposing it to an email adapter.
 *
 * <p>Lifecycle changes made after scheduling make the request obsolete. This prevents queued email
 * from being sent after acceptance, rejection, revocation, expiration, or a newer resend.
 */
@Component
@RequiredArgsConstructor
public final class DeliverWorkspaceInvitationEmailService
    implements DeliverWorkspaceInvitationEmailUseCase {

  private final @NonNull WorkspaceInvitationRepository invitationRepository;
  private final @NonNull WorkspaceRepository workspaceRepository;
  private final @NonNull AccountUserRepository accountUserRepository;
  private final @NonNull WorkspaceInvitationEmailSender emailSender;
  private final @NonNull Clock clock;

  @Override
  public void deliver(@NonNull WorkspaceInvitationDeliveryRequest request) {
    requireNonNull(request, "workspace invitation delivery request cannot be null");
    final var invitation = invitationRepository.findById(request.invitationId()).orElse(null); // NOSONAR
    if (invitation == null
        || invitation.effectiveStatus(clock.instant()) != PENDING
        || invitation.resendCount() != request.resendCount()) {
      return;
    }

    final var workspace = workspaceRepository.findById(invitation.workspaceId())
        .orElseThrow(() -> new WorkspaceNotFoundException(invitation.workspaceId()));
    final var inviter = accountUserRepository.findById(invitation.invitedByUserId())
        .orElseThrow(() -> new WorkspaceInvitationAuthorNotFoundException(invitation.invitedByUserId()));
    if (!workspace.isActive()) {
      return;
    }

    emailSender.send(new WorkspaceInvitationEmail(
        invitation.invitationId(),
        invitation.invitedEmail(),
        workspace.name(),
        invitation.role(),
        inviter.displayName(),
        invitation.expiresAt()
    ));
  }
}
