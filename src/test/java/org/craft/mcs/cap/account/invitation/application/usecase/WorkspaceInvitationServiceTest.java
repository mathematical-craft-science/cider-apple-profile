package org.craft.mcs.cap.account.invitation.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.craft.mcs.cap.core.account.audit.application.usecase.WorkspaceAdministrativeAuditAppender;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeSubjectId;
import org.craft.mcs.cap.core.account.invitation.application.WorkspaceInvitationConflictException;
import org.craft.mcs.cap.core.account.invitation.application.model.WorkspaceInvitationDefaults;
import org.craft.mcs.cap.core.account.invitation.application.port.out.WorkspaceInvitationRepository;
import org.craft.mcs.cap.core.account.invitation.application.usecase.WorkspaceInvitationService;
import org.craft.mcs.cap.core.account.invitation.domain.model.WorkspaceInvitation;
import org.craft.mcs.cap.core.account.membership.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.membership.application.usecase.WorkspacePermissionChecker;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceAdministrationLock;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.account.support.DirectTransactionHelper.INSTANCE;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.INVITATION_ACCEPTED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeSubjectType.INVITATION;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipStatus.ACTIVE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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
    final var auditAppender = mock(WorkspaceAdministrativeAuditAppender.class);
    final var user = accountUser();
    final var invitation = WorkspaceInvitation.create(
        workspace.workspaceId(),
        user.email(),
        AccountRole.TECHNOLOGIST,
        user.userId(),
        NOW,
        Duration.ofDays(7)
    );

    when(userResolver.resolve()).thenReturn(user);
    when(invitationRepository.findById(invitation.invitationId())).thenReturn(Optional.of(invitation));
    when(workspaceRepository.findById(workspace.workspaceId())).thenReturn(Optional.of(workspace));
    when(membershipRepository.findByWorkspaceIdAndUserId(workspace.workspaceId(), user.userId()))
        .thenReturn(Optional.empty());
    when(invitationRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0, WorkspaceInvitation.class));
    when(membershipRepository.save(any()))
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
            auditAppender,
            new WorkspaceInvitationDefaults(Duration.ofDays(7)),
            INSTANCE,
            CLOCK
        ),
        invitationRepository,
        administrationLock,
        membershipRepository,
        auditAppender,
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
    verify(fixture.membershipRepository()).save(membershipCaptor.capture());
    final var mutationOrder = inOrder(fixture.invitationRepository(), fixture.administrationLock());
    mutationOrder.verify(fixture.invitationRepository()).findById(fixture.invitation().invitationId());
    mutationOrder.verify(fixture.administrationLock()).acquire(fixture.invitation().workspaceId());
    mutationOrder.verify(fixture.invitationRepository()).findById(fixture.invitation().invitationId());

    assertThat(result)
        .returns(fixture.invitation().workspaceId(), view -> view.workspace().workspaceId())
        .returns(AccountRole.TECHNOLOGIST, WorkspaceMembershipView::role)
        .returns(ACTIVE, WorkspaceMembershipView::status)
        .satisfies(_ -> assertThat(membershipCaptor.getValue())
            .returns(fixture.user().userId(), WorkspaceMembership::userId)
            .returns(fixture.invitation().workspaceId(), WorkspaceMembership::workspaceId)
            .returns(AccountRole.TECHNOLOGIST, WorkspaceMembership::role)
            .matches(WorkspaceMembership::isActive));
    verify(fixture.auditAppender()).append(
        fixture.invitation().workspaceId(),
        fixture.user().userId(),
        INVITATION_ACCEPTED,
        INVITATION,
        WorkspaceAdministrativeSubjectId.forInvitation(fixture.invitation().invitationId()),
        WorkspaceAdministrativeAuditValue.of("PENDING"),
        WorkspaceAdministrativeAuditValue.of("ACCEPTED")
    );
  }

  @Test
  void rejectsAcceptanceForArchivedWorkspaceBeforeCreatingMembership() {
    final var archivedWorkspace = Workspace.create(DisplayName.of("Archived"), WorkspaceType.CIDERY).archive();
    final var fixture = fixture(archivedWorkspace);

    assertThatThrownBy(() -> fixture.service().accept(fixture.invitation().invitationId()))
        .isInstanceOf(WorkspaceInvitationConflictException.class)
        .hasMessage("workspace invitation belongs to an archived workspace");
    verify(fixture.membershipRepository(), never()).save(any());
  }

  private record InvitationFixture(
      @NonNull WorkspaceInvitationService service,
      @NonNull WorkspaceInvitationRepository invitationRepository,
      @NonNull WorkspaceAdministrationLock administrationLock,
      @NonNull WorkspaceMembershipRepository membershipRepository,
      @NonNull WorkspaceAdministrativeAuditAppender auditAppender,
      @NonNull AccountUser user,
      @NonNull WorkspaceInvitation invitation
  ) {
  }
}
