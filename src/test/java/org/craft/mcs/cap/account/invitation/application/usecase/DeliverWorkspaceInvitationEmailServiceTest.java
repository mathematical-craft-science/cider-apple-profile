package org.craft.mcs.cap.account.invitation.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.stream.Stream;
import org.craft.mcs.cap.core.account.invitation.application.model.WorkspaceInvitationDeliveryRequest;
import org.craft.mcs.cap.core.account.invitation.application.model.WorkspaceInvitationEmail;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationEmailSender;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationRepository;
import org.craft.mcs.cap.core.account.invitation.application.usecase.DeliverWorkspaceInvitationEmailService;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUserStatus;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceStatus;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DeliverWorkspaceInvitationEmailServiceTest {

  private static final Instant NOW = Instant.parse("2026-07-14T09:00:00Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  @Test
  void sendsCurrentPendingInvitation() {
    final var fixture = fixture(invitation());
    final var emailCaptor = ArgumentCaptor.forClass(WorkspaceInvitationEmail.class);

    fixture.service().deliver(WorkspaceInvitationDeliveryRequest.from(fixture.invitation()));

    verify(fixture.emailSender()).send(emailCaptor.capture());
    assertThat(emailCaptor.getValue())
        .returns(fixture.invitation().invitationId(), WorkspaceInvitationEmail::invitationId)
        .returns(fixture.invitation().invitedEmail(), WorkspaceInvitationEmail::recipient)
        .returns(fixture.workspace().name(), WorkspaceInvitationEmail::workspaceName)
        .returns(fixture.invitation().role(), WorkspaceInvitationEmail::role)
        .returns(fixture.inviter().displayName(), WorkspaceInvitationEmail::inviterName)
        .returns(fixture.invitation().expiresAt(), WorkspaceInvitationEmail::expiresAt);
  }

  @ParameterizedTest
  @MethodSource("completedInvitations")
  void ignoresInvitationThatIsNoLongerPending(@NonNull WorkspaceInvitation completedInvitation) {
    final var fixture = fixture(completedInvitation);

    fixture.service().deliver(WorkspaceInvitationDeliveryRequest.from(completedInvitation));

    verifyNoInteractions(fixture.emailSender());
  }

  @Test
  void ignoresDeliverySupersededByNewerResend() {
    final var resent = invitation().resend(NOW, Duration.ofDays(7));
    final var fixture = fixture(resent);
    final var obsoleteRequest = new WorkspaceInvitationDeliveryRequest(
        resent.invitationId(),
        resent.resendCount() - 1
    );

    fixture.service().deliver(obsoleteRequest);

    verifyNoInteractions(fixture.emailSender());
  }

  @Test
  void ignoresInvitationThatExpiredBeforeDelivery() {
    final var invitation = invitation();
    final var expiredClock = Clock.fixed(invitation.expiresAt(), ZoneOffset.UTC);
    final var fixture = fixture(invitation, expiredClock);

    fixture.service().deliver(WorkspaceInvitationDeliveryRequest.from(invitation));

    verifyNoInteractions(fixture.emailSender());
  }

  @Test
  void sendsInvitationCreatedByAnInactiveAccount() {
    final var fixture = fixture(invitation(), CLOCK, AccountUserStatus.DISABLED);

    fixture.service().deliver(WorkspaceInvitationDeliveryRequest.from(fixture.invitation()));

    verify(fixture.emailSender()).send(new WorkspaceInvitationEmail(
        fixture.invitation().invitationId(),
        fixture.invitation().invitedEmail(),
        fixture.workspace().name(),
        fixture.invitation().role(),
        fixture.inviter().displayName(),
        fixture.invitation().expiresAt()
    ));
  }

  private static @NonNull DeliveryFixture fixture(@NonNull WorkspaceInvitation invitation) {
    return fixture(invitation, CLOCK);
  }

  private static @NonNull DeliveryFixture fixture(
      @NonNull WorkspaceInvitation invitation,
      @NonNull Clock clock
  ) {
    return fixture(invitation, clock, AccountUserStatus.ACTIVE);
  }

  private static @NonNull DeliveryFixture fixture(
      @NonNull WorkspaceInvitation invitation,
      @NonNull Clock clock,
      @NonNull AccountUserStatus inviterStatus
  ) {
    final var invitationRepository = mock(WorkspaceInvitationRepository.class);
    final var workspaceRepository = mock(WorkspaceRepository.class);
    final var accountUserRepository = mock(AccountUserRepository.class);
    final var emailSender = mock(WorkspaceInvitationEmailSender.class);
    final var workspace = new Workspace(
        invitation.workspaceId(),
        DisplayName.of("Summer Cidery"),
        WorkspaceType.CIDERY,
        WorkspaceStatus.ACTIVE,
        null,
        null
    );
    final var inviter = new AccountUser(
        invitation.invitedByUserId(),
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_inviter"),
        EmailAddress.of("owner@example.com"),
        DisplayName.of("Workspace Owner"),
        null,
        inviterStatus
    );

    when(invitationRepository.findById(invitation.invitationId())).thenReturn(Optional.of(invitation));
    when(workspaceRepository.findById(invitation.workspaceId())).thenReturn(Optional.of(workspace));
    when(accountUserRepository.findById(invitation.invitedByUserId())).thenReturn(Optional.of(inviter));

    return new DeliveryFixture(
        new DeliverWorkspaceInvitationEmailService(
            invitationRepository,
            workspaceRepository,
            accountUserRepository,
            emailSender,
            clock
        ),
        emailSender,
        invitation,
        workspace,
        inviter
    );
  }

  private static @NonNull WorkspaceInvitation invitation() {
    final var inviter = inviter();
    final var workspace = Workspace.create(DisplayName.of("Summer Cidery"), WorkspaceType.CIDERY);
    return WorkspaceInvitation.create(
        workspace.workspaceId(),
        EmailAddress.of("invitee@example.com"),
        AccountRole.TECHNOLOGIST,
        inviter.userId(),
        NOW,
        Duration.ofDays(7)
    );
  }

  private static @NonNull Stream<@NonNull WorkspaceInvitation> completedInvitations() {
    final var accepted = invitation();
    final var rejected = invitation();
    final var revoked = invitation();
    final var expired = invitation();
    return Stream.of(
        accepted.accept(AccountUserId.newId(), NOW),
        rejected.reject(NOW),
        revoked.revoke(NOW),
        expired.expire(expired.expiresAt())
    );
  }

  private static @NonNull AccountUser inviter() {
    return AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_inviter"),
        EmailAddress.of("owner@example.com"),
        DisplayName.of("Workspace Owner"),
        null
    );
  }

  private record DeliveryFixture(
      @NonNull DeliverWorkspaceInvitationEmailService service,
      @NonNull WorkspaceInvitationEmailSender emailSender,
      @NonNull WorkspaceInvitation invitation,
      @NonNull Workspace workspace,
      @NonNull AccountUser inviter
  ) {
  }
}
