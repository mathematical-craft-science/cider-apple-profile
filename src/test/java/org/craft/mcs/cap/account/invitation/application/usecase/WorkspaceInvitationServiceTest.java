package org.craft.mcs.cap.account.invitation.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.craft.mcs.cap.core.account.invitation.application.WorkspaceInvitationConflictException;
import org.craft.mcs.cap.core.account.invitation.application.WorkspaceInvitationRecipientMismatchException;
import org.craft.mcs.cap.core.account.invitation.application.model.WorkspaceInvitationDeliveryRequest;
import org.craft.mcs.cap.core.account.invitation.application.model.WorkspaceInvitationDefaults;
import org.craft.mcs.cap.core.account.invitation.application.port.in.InviteWorkspaceMemberCommand;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationCommand;
import org.craft.mcs.cap.core.account.invitation.application.port.in.WorkspaceInvitationView;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationDeliveryScheduler;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationRepository;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationAuditTrail;
import org.craft.mcs.cap.core.account.invitation.application.usecase.WorkspaceInvitationService;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.context.application.model.ResolvedWorkspaceContext;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.context.application.usecase.WorkspacePermissionChecker;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.application.port.out.WorkspaceAdministrationLock;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.account.support.DirectTransactionHelper.INSTANCE;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipStatus.ACTIVE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.craft.mcs.cap.support.VersionedAggregateFixtures.versioned;


class WorkspaceInvitationServiceTest {

  private static final Instant NOW = Instant.parse("2026-07-14T09:00:00Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  private static @NonNull InvitationFixture fixture(@NonNull Workspace workspace) {
    final var userResolver = mock(CurrentAccountUserResolver.class);
    final var permissionChecker = mock(WorkspacePermissionChecker.class);
    final var userRepository = mock(AccountUserRepository.class);
    final var workspaceRepository = mock(WorkspaceRepository.class);
    final var membershipRepository = mock(WorkspaceMembershipRepository.class);
    final var invitationRepository = mock(WorkspaceInvitationRepository.class);
    final var administrationLock = mock(WorkspaceAdministrationLock.class);
    final var auditTrail = mock(WorkspaceInvitationAuditTrail.class);
    final var deliveryScheduler = mock(WorkspaceInvitationDeliveryScheduler.class);
    final var user = accountUser();
    final var workspaceContext = mock(ResolvedWorkspaceContext.class);
    final var invitation = WorkspaceInvitation.create(
        workspace.workspaceId(),
        user.email(),
        AccountRole.TECHNOLOGIST,
        user.userId(),
        NOW,
        Duration.ofDays(7)
    );

    when(userResolver.resolve()).thenReturn(user);
    when(workspaceContext.user()).thenReturn(user);
    when(permissionChecker.requirePermission(eq(workspace.workspaceId()), any()))
        .thenReturn(workspaceContext);
    when(invitationRepository.findByWorkspaceIdAndInvitedEmailAndStatus(any(), any(), any()))
        .thenReturn(Optional.empty());
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
    when(invitationRepository.findVersionedById(invitation.invitationId()))
        .thenReturn(Optional.of(versioned(invitation)));
    when(workspaceRepository.findById(workspace.workspaceId())).thenReturn(Optional.of(workspace));
    when(membershipRepository.findVersionedByWorkspaceIdAndUserId(workspace.workspaceId(), user.userId()))
        .thenReturn(Optional.empty());
    when(invitationRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(invitationRepository.create(any()))
        .thenAnswer(invocation -> invocation.getArgument(0, WorkspaceInvitation.class));
    when(membershipRepository.create(any()))
        .thenAnswer(invocation -> invocation.getArgument(0, WorkspaceMembership.class));

    return new InvitationFixture(
        new WorkspaceInvitationService(
            userResolver,
            permissionChecker,
            userRepository,
            workspaceRepository,
            membershipRepository,
            invitationRepository,
            administrationLock,
            auditTrail,
            deliveryScheduler,
            new WorkspaceInvitationDefaults(Duration.ofDays(7)),
            INSTANCE,
            CLOCK
        ),
        invitationRepository,
        administrationLock,
        membershipRepository,
        userRepository,
        auditTrail,
        deliveryScheduler,
        user,
        invitation
    );
  }

  private static @NonNull AccountUser accountUser() {
    return AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_invited"),
        EmailAddress.of("Technologist@Example.com"),
        DisplayName.of("Technologist"),
        null
    );
  }

  @Test
  void acceptsInvitationAndCreatesMembershipWithAuditAtomically() {
    final var fixture = fixture(Workspace.create(DisplayName.of("Cidery"), WorkspaceType.CIDERY));

    final var result = fixture.service().accept(fixture.invitation().invitationId());
    final var membershipCaptor = ArgumentCaptor.forClass(WorkspaceMembership.class);
    verify(fixture.membershipRepository()).create(membershipCaptor.capture());
    final var mutationOrder = inOrder(fixture.invitationRepository(), fixture.administrationLock());
    mutationOrder.verify(fixture.invitationRepository()).findVersionedById(fixture.invitation().invitationId());
    mutationOrder.verify(fixture.administrationLock()).acquire(fixture.invitation().workspaceId());
    mutationOrder.verify(fixture.invitationRepository()).findVersionedById(fixture.invitation().invitationId());

    assertThat(result)
        .returns(fixture.invitation().workspaceId(), view -> view.workspace().workspaceId())
        .returns(AccountRole.TECHNOLOGIST, WorkspaceMembershipView::role)
        .returns(ACTIVE, WorkspaceMembershipView::status)
        .satisfies(_ -> assertThat(membershipCaptor.getValue())
            .returns(fixture.user().userId(), WorkspaceMembership::userId)
            .returns(fixture.invitation().workspaceId(), WorkspaceMembership::workspaceId)
            .returns(AccountRole.TECHNOLOGIST, WorkspaceMembership::role)
            .matches(WorkspaceMembership::isActive));
    verify(fixture.auditTrail()).recordAccepted(
        fixture.user().userId(),
        fixture.invitation().accept(fixture.user().userId(), NOW)
    );
  }

  @Test
  void rejoinsRemovedMembershipWithRoleFromAcceptedInvitation() {
    final var fixture = fixture(Workspace.create(DisplayName.of("Cidery"), WorkspaceType.CIDERY));
    final var removedMembership = WorkspaceMembership.create(
        fixture.invitation().workspaceId(),
        fixture.user().userId(),
        AccountRole.VIEWER
    ).remove();
    when(fixture.membershipRepository().findVersionedByWorkspaceIdAndUserId(
        fixture.invitation().workspaceId(),
        fixture.user().userId()
    )).thenReturn(Optional.of(versioned(removedMembership)));
    when(fixture.membershipRepository().update(any())).thenAnswer(invocation -> invocation.getArgument(0));

    final var result = fixture.service().accept(fixture.invitation().invitationId());
    final ArgumentCaptor<VersionedAggregate<WorkspaceMembership>> membershipCaptor = ArgumentCaptor.captor();
    verify(fixture.membershipRepository()).update(membershipCaptor.capture());
    verify(fixture.membershipRepository(), never()).create(any());

    assertThat(result)
        .returns(removedMembership.membershipId(), WorkspaceMembershipView::membershipId)
        .returns(AccountRole.TECHNOLOGIST, WorkspaceMembershipView::role)
        .returns(ACTIVE, WorkspaceMembershipView::status)
        .satisfies(_ -> assertThat(membershipCaptor.getValue().aggregate())
            .returns(removedMembership.membershipId(), WorkspaceMembership::membershipId)
            .returns(AccountRole.TECHNOLOGIST, WorkspaceMembership::role)
            .matches(WorkspaceMembership::isActive));
  }

  @Test
  void permitsNewInvitationForRemovedWorkspaceMembership() {
    final var workspace = Workspace.create(DisplayName.of("Cidery"), WorkspaceType.CIDERY);
    final var fixture = fixture(workspace);
    final var removedMembership = WorkspaceMembership.create(
        workspace.workspaceId(),
        fixture.user().userId(),
        AccountRole.VIEWER
    ).remove();
    when(fixture.userRepository().findByEmail(fixture.user().email())).thenReturn(Optional.of(fixture.user()));
    when(fixture.membershipRepository().findVersionedByWorkspaceIdAndUserId(
        workspace.workspaceId(),
        fixture.user().userId()
    )).thenReturn(Optional.of(versioned(removedMembership)));
    final var command = new InviteWorkspaceMemberCommand(
        workspace.workspaceId(),
        fixture.user().email(),
        AccountRole.TECHNOLOGIST
    );

    final var result = fixture.service().invite(command);

    assertThat(result)
        .returns(workspace.workspaceId(), WorkspaceInvitationView::workspaceId)
        .returns(fixture.user().email(), WorkspaceInvitationView::invitedEmail)
        .returns(AccountRole.TECHNOLOGIST, WorkspaceInvitationView::role);
    verify(fixture.invitationRepository()).create(any(WorkspaceInvitation.class));
  }

  @Test
  void rejectsInvitationAcceptanceForCurrentWorkspaceMembership() {
    final var workspace = Workspace.create(DisplayName.of("Cidery"), WorkspaceType.CIDERY);
    final var fixture = fixture(workspace);
    final var membership = WorkspaceMembership.create(
        workspace.workspaceId(),
        fixture.user().userId(),
        AccountRole.VIEWER
    );
    when(fixture.membershipRepository().findVersionedByWorkspaceIdAndUserId(
        workspace.workspaceId(),
        fixture.user().userId()
    )).thenReturn(Optional.of(versioned(membership)));

    assertThatThrownBy(() -> fixture.service().accept(fixture.invitation().invitationId()))
        .isInstanceOf(WorkspaceInvitationConflictException.class)
        .hasMessage("account is already a workspace member");
    verify(fixture.invitationRepository(), never()).update(any());
    verify(fixture.membershipRepository(), never()).update(any());
    verify(fixture.membershipRepository(), never()).create(any());
  }

  @Test
  void schedulesInitialInvitationGenerationInsideInviteUseCase() {
    final var workspace = Workspace.create(DisplayName.of("Cidery"), WorkspaceType.CIDERY);
    final var fixture = fixture(workspace);
    final var command = new InviteWorkspaceMemberCommand(
        workspace.workspaceId(),
        EmailAddress.of("invitee@example.com"),
        AccountRole.TECHNOLOGIST
    );

    final var result = fixture.service().invite(command);
    final var deliveryCaptor = ArgumentCaptor.forClass(WorkspaceInvitationDeliveryRequest.class);
    verify(fixture.deliveryScheduler()).schedule(deliveryCaptor.capture());

    assertThat(deliveryCaptor.getValue())
        .returns(result.invitationId(), WorkspaceInvitationDeliveryRequest::invitationId)
        .returns(0, WorkspaceInvitationDeliveryRequest::resendCount);
  }

  @Test
  void schedulesRenewedInvitationGenerationInsideResendUseCase() {
    final var workspace = Workspace.create(DisplayName.of("Cidery"), WorkspaceType.CIDERY);
    final var fixture = fixture(workspace);
    final var command = new WorkspaceInvitationCommand(
        workspace.workspaceId(),
        fixture.invitation().invitationId()
    );

    fixture.service().resend(command);
    final var deliveryCaptor = ArgumentCaptor.forClass(WorkspaceInvitationDeliveryRequest.class);
    verify(fixture.deliveryScheduler()).schedule(deliveryCaptor.capture());

    assertThat(deliveryCaptor.getValue())
        .returns(fixture.invitation().invitationId(), WorkspaceInvitationDeliveryRequest::invitationId)
        .returns(1, WorkspaceInvitationDeliveryRequest::resendCount);
  }

  @Test
  void rejectsAcceptanceForArchivedWorkspaceBeforeCreatingMembership() {
    final var archivedWorkspace = Workspace.create(DisplayName.of("Archived"), WorkspaceType.CIDERY).archive();
    final var fixture = fixture(archivedWorkspace);

    assertThatThrownBy(() -> fixture.service().accept(fixture.invitation().invitationId()))
        .isInstanceOf(WorkspaceInvitationConflictException.class)
        .hasMessage("workspace invitation belongs to an archived workspace");
    verify(fixture.membershipRepository(), never()).create(any());
  }

  @Test
  void rejectsAcceptanceWhenAuthenticatedAccountIsNotTheInvitationRecipient() {
    final var workspace = Workspace.create(DisplayName.of("Cidery"), WorkspaceType.CIDERY);
    final var fixture = fixture(workspace);
    final var invitationForAnotherAccount = WorkspaceInvitation.create(
        workspace.workspaceId(),
        EmailAddress.of("another@example.com"),
        AccountRole.TECHNOLOGIST,
        fixture.user().userId(),
        NOW,
        Duration.ofDays(7)
    );
    when(fixture.invitationRepository().findVersionedById(invitationForAnotherAccount.invitationId()))
        .thenReturn(Optional.of(versioned(invitationForAnotherAccount)));

    assertThatThrownBy(() -> fixture.service().accept(invitationForAnotherAccount.invitationId()))
        .isInstanceOf(WorkspaceInvitationRecipientMismatchException.class)
        .hasMessage("workspace invitation was issued to a different account email");
    verify(fixture.invitationRepository(), never()).update(any());
    verify(fixture.membershipRepository(), never()).create(any());
  }

  private record InvitationFixture(
      @NonNull WorkspaceInvitationService service,
      @NonNull WorkspaceInvitationRepository invitationRepository,
      @NonNull WorkspaceAdministrationLock administrationLock,
      @NonNull WorkspaceMembershipRepository membershipRepository,
      @NonNull AccountUserRepository userRepository,
      @NonNull WorkspaceInvitationAuditTrail auditTrail,
      @NonNull WorkspaceInvitationDeliveryScheduler deliveryScheduler,
      @NonNull AccountUser user,
      @NonNull WorkspaceInvitation invitation
  ) {
  }
}
