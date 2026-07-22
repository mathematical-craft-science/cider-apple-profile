package org.craft.mcs.cap.account.invitation.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.account.invitation.application.port.in.IncomingWorkspaceInvitationView;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationInviterView;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationRepository;
import org.craft.mcs.cap.core.account.invitation.application.usecase.IncomingWorkspaceInvitationQueryService;
import org.craft.mcs.cap.core.account.invitation.application.WorkspaceInvitationRecipientMismatchException;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitationStatus;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceView;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class IncomingWorkspaceInvitationQueryServiceTest {

  private static final Instant NOW = Instant.parse("2026-07-15T09:00:00Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  @Test
  void returnsRecipientFacingInvitationPreview() {
    final var currentUser = account("recipient", "recipient@example.com", "Recipient");
    final var inviter = account("owner", "owner@example.com", "Cidery Owner");
    final var workspace = Workspace.create(DisplayName.of("North Orchard"), WorkspaceType.CIDERY);
    final var invitation = invitation(workspace, currentUser, inviter);
    final var fixture = fixture(currentUser, inviter, workspace, invitation);

    final var result = fixture.service().get(invitation.invitationId());

    assertThat(result)
        .returns(invitation.invitationId(), IncomingWorkspaceInvitationView::invitationId)
        .returns(AccountRole.TECHNOLOGIST, IncomingWorkspaceInvitationView::role)
        .returns(WorkspaceInvitationStatus.PENDING, IncomingWorkspaceInvitationView::status)
        .returns(invitation.expiresAt(), IncomingWorkspaceInvitationView::expiresAt)
        .satisfies(value -> assertThat(value.workspace())
            .returns(workspace.workspaceId(), WorkspaceView::workspaceId)
            .returns(workspace.name(), WorkspaceView::name))
        .satisfies(value -> assertThat(value.invitedBy())
            .returns(inviter.userId(), WorkspaceInvitationInviterView::userId)
            .returns(inviter.displayName(), WorkspaceInvitationInviterView::displayName));
  }

  @Test
  void listsInvitationsAddressedToCurrentAccount() {
    final var currentUser = account("recipient", "recipient@example.com", "Recipient");
    final var inviter = account("owner", "owner@example.com", "Cidery Owner");
    final var workspace = Workspace.create(DisplayName.of("North Orchard"), WorkspaceType.CIDERY);
    final var invitation = invitation(workspace, currentUser, inviter);
    final var fixture = fixture(currentUser, inviter, workspace, invitation);

    final var result = fixture.service().list();

    assertThat(result)
        .singleElement()
        .returns(invitation.invitationId(), IncomingWorkspaceInvitationView::invitationId)
        .returns(AccountRole.TECHNOLOGIST, IncomingWorkspaceInvitationView::role)
        .returns(WorkspaceInvitationStatus.PENDING, IncomingWorkspaceInvitationView::status)
        .satisfies(value -> assertThat(value.workspace())
            .returns(workspace.workspaceId(), WorkspaceView::workspaceId)
            .returns(workspace.name(), WorkspaceView::name))
        .satisfies(value -> assertThat(value.invitedBy())
            .returns(inviter.userId(), WorkspaceInvitationInviterView::userId)
            .returns(inviter.displayName(), WorkspaceInvitationInviterView::displayName));
  }

  @Test
  void rejectsPreviewForAnotherAccountBeforeReadingContext() {
    final var currentUser = account("other", "other@example.com", "Other User");
    final var recipient = account("recipient", "recipient@example.com", "Recipient");
    final var inviter = account("owner", "owner@example.com", "Cidery Owner");
    final var workspace = Workspace.create(DisplayName.of("North Orchard"), WorkspaceType.CIDERY);
    final var invitation = invitation(workspace, recipient, inviter);
    final var fixture = fixture(currentUser, inviter, workspace, invitation);

    assertThatThrownBy(() -> fixture.service().get(invitation.invitationId()))
        .isInstanceOf(WorkspaceInvitationRecipientMismatchException.class)
        .hasMessage("workspace invitation was issued to a different account email");
    verifyNoInteractions(fixture.workspaceRepository());
    verify(fixture.accountUserRepository(), never()).findById(inviter.userId());
  }

  private static @NonNull PreviewFixture fixture(
      @NonNull AccountUser currentUser,
      @NonNull AccountUser inviter,
      @NonNull Workspace workspace,
      @NonNull WorkspaceInvitation invitation
  ) {
    final var currentUserResolver = mock(CurrentAccountUserResolver.class);
    final var invitationRepository = mock(WorkspaceInvitationRepository.class);
    final var workspaceRepository = mock(WorkspaceRepository.class);
    final var accountUserRepository = mock(AccountUserRepository.class);
    when(currentUserResolver.resolve()).thenReturn(currentUser);
    when(invitationRepository.findById(invitation.invitationId())).thenReturn(Optional.of(invitation));
    when(invitationRepository.findAllByInvitedEmail(currentUser.email())).thenReturn(List.of(invitation));
    when(workspaceRepository.findById(workspace.workspaceId())).thenReturn(Optional.of(workspace));
    when(accountUserRepository.findById(inviter.userId())).thenReturn(Optional.of(inviter));
    return new PreviewFixture(
        new IncomingWorkspaceInvitationQueryService(
            currentUserResolver,
            invitationRepository,
            workspaceRepository,
            accountUserRepository,
            CLOCK
        ),
        workspaceRepository,
        accountUserRepository
    );
  }

  private static @NonNull WorkspaceInvitation invitation(
      @NonNull Workspace workspace,
      @NonNull AccountUser recipient,
      @NonNull AccountUser inviter
  ) {
    return WorkspaceInvitation.create(
        workspace.workspaceId(),
        recipient.email(),
        AccountRole.TECHNOLOGIST,
        inviter.userId(),
        NOW,
        Duration.ofDays(7)
    );
  }

  private static @NonNull AccountUser account(
      @NonNull String subject,
      @NonNull String email,
      @NonNull String displayName
  ) {
    return AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of(subject),
        EmailAddress.of(email),
        DisplayName.of(displayName),
        null
    );
  }

  private record PreviewFixture(
      @NonNull IncomingWorkspaceInvitationQueryService service,
      @NonNull WorkspaceRepository workspaceRepository,
      @NonNull AccountUserRepository accountUserRepository
  ) {
  }
}
