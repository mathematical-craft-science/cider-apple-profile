package org.craft.mcs.cap.account.invitation.domain.model;

import java.time.Duration;
import java.time.Instant;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationRuleException;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkspaceInvitationTest {

  private static final Instant NOW = Instant.parse("2026-07-14T08:00:00Z");

  @Test
  void representsExpirationWithoutCreatingMembershipState() {
    final var invitation = invitation(AccountRole.TECHNOLOGIST);

    assertThat(invitation)
        .returns(WorkspaceInvitationStatus.PENDING, value -> value.effectiveStatus(NOW))
        .returns(
            WorkspaceInvitationStatus.EXPIRED,
            value -> value.effectiveStatus(NOW.plus(Duration.ofDays(8)))
        )
        .returns(EmailAddress.of("technologist@example.com"), WorkspaceInvitation::invitedEmail)
        .returns(0, WorkspaceInvitation::resendCount);
  }

  @Test
  void renewsAnExpiredInvitationForAnotherDeliveryAttempt() {
    final var invitation = invitation(AccountRole.VIEWER);
    final var resentAt = NOW.plus(Duration.ofDays(8));

    assertThat(invitation.resend(resentAt, Duration.ofDays(7)))
        .returns(WorkspaceInvitationStatus.PENDING, WorkspaceInvitation::status)
        .returns(1, WorkspaceInvitation::resendCount)
        .returns(resentAt.plus(Duration.ofDays(7)), WorkspaceInvitation::expiresAt);
  }

  @Test
  void preventsOwnerAssignmentOutsideOwnershipTransfer() {
    assertThatThrownBy(() -> invitation(AccountRole.OWNER))
        .isInstanceOf(WorkspaceInvitationRuleException.class)
        .hasMessage("OWNER role can be assigned only by ownership transfer");
  }

  @Test
  void requiresAcceptingAccountForAcceptedInvitation() {
    final var invitation = invitation(AccountRole.TECHNOLOGIST);

    assertThatThrownBy(() -> restore(invitation, WorkspaceInvitationStatus.ACCEPTED, null))
        .isInstanceOf(WorkspaceInvitationRuleException.class)
        .hasMessage("accepted invitation must identify the accepting account");
  }

  @Test
  void rejectsAcceptingAccountForNonAcceptedInvitation() {
    final var invitation = invitation(AccountRole.TECHNOLOGIST);
    final var acceptingUserId = AccountUserId.newId();

    assertThatThrownBy(() -> restore(
        invitation,
        WorkspaceInvitationStatus.PENDING,
        acceptingUserId
    ))
        .isInstanceOf(WorkspaceInvitationRuleException.class)
        .hasMessage("only an accepted invitation can identify an accepting account");
  }

  private static @NonNull WorkspaceInvitation invitation(@NonNull AccountRole role) {
    return WorkspaceInvitation.create(
        WorkspaceId.newId(),
        EmailAddress.of("Technologist@Example.com"),
        role,
        AccountUserId.newId(),
        NOW,
        Duration.ofDays(7)
    );
  }

  private static @NonNull WorkspaceInvitation restore(
      @NonNull WorkspaceInvitation invitation,
      @NonNull WorkspaceInvitationStatus status,
      @Nullable AccountUserId acceptedByUserId
  ) {
    return new WorkspaceInvitation(
        invitation.invitationId(),
        invitation.workspaceId(),
        invitation.invitedEmail(),
        invitation.role(),
        status,
        invitation.invitedByUserId(),
        acceptedByUserId,
        invitation.expiresAt(),
        invitation.resendCount()
    );
  }
}
