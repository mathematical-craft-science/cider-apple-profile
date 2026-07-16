package org.craft.mcs.cap.account.administration.application.usecase;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.account.context.application.model.ResolvedWorkspaceContext;
import org.craft.mcs.cap.core.account.administration.application.port.in.TransferWorkspaceOwnershipCommand;
import org.craft.mcs.cap.core.account.administration.application.port.in.WorkspaceMemberView;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipAuditTrail;
import org.craft.mcs.cap.core.account.administration.application.usecase.WorkspaceMembershipManagementService;
import org.craft.mcs.cap.core.account.context.application.usecase.WorkspacePermissionChecker;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceView;
import org.craft.mcs.cap.core.account.shared.application.port.out.WorkspaceAdministrationLock;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.craft.mcs.cap.account.support.DirectTransactionHelper.INSTANCE;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.WORKSPACE_OWNERSHIP_TRANSFER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.craft.mcs.cap.support.VersionedAggregateFixtures.versioned;


class WorkspaceMembershipManagementServiceTest {

  private static @NonNull AccountUser accountUser(
      @NonNull String subject,
      @NonNull String email
  ) {
    return AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of(subject),
        EmailAddress.of(email),
        DisplayName.of(subject),
        null
    );
  }

  @Test
  void transfersOwnershipAndPersistsBothMembershipChangesAtomically() {
    final var permissionChecker = mock(WorkspacePermissionChecker.class);
    final var userRepository = mock(AccountUserRepository.class);
    final var membershipRepository = mock(WorkspaceMembershipRepository.class);
    final var administrationLock = mock(WorkspaceAdministrationLock.class);
    final var auditTrail = mock(WorkspaceMembershipAuditTrail.class);
    final var workspace = Workspace.create(DisplayName.of("Cidery"), WorkspaceType.CIDERY);
    final var currentOwnerUser = accountUser("owner", "owner@example.com");
    final var targetUser = accountUser("target", "target@example.com");
    final var currentOwner = WorkspaceMembership.createOwner(workspace.workspaceId(), currentOwnerUser.userId());
    final var target = WorkspaceMembership.create(
        workspace.workspaceId(),
        targetUser.userId(),
        AccountRole.ADMIN
    );
    final var activeMembership = new WorkspaceMembershipView(
        currentOwner.membershipId(),
        new WorkspaceView(workspace.workspaceId(), workspace.name(), workspace.type(), workspace.status()),
        currentOwner.role(),
        currentOwner.status()
    );
    final var context = new ResolvedWorkspaceContext(
        currentOwnerUser,
        List.of(activeMembership),
        activeMembership
    );
    final var command = new TransferWorkspaceOwnershipCommand(
        workspace.workspaceId(),
        target.membershipId()
    );

    when(permissionChecker.requirePermission(workspace.workspaceId(), WORKSPACE_OWNERSHIP_TRANSFER))
        .thenReturn(context);
    when(membershipRepository.findVersionedById(currentOwner.membershipId()))
        .thenReturn(Optional.of(versioned(currentOwner)));
    when(membershipRepository.findVersionedById(target.membershipId()))
        .thenReturn(Optional.of(versioned(target)));
    when(membershipRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(userRepository.findById(targetUser.userId())).thenReturn(Optional.of(targetUser));

    final var service = new WorkspaceMembershipManagementService(
        permissionChecker,
        userRepository,
        membershipRepository,
        administrationLock,
        auditTrail,
        INSTANCE
    );

    final var result = service.transferOwnership(command);
    final ArgumentCaptor<VersionedAggregate<WorkspaceMembership>> membershipCaptor = ArgumentCaptor.captor();
    verify(membershipRepository, times(2)).update(membershipCaptor.capture());
    final var mutationOrder = inOrder(administrationLock, permissionChecker);
    mutationOrder.verify(administrationLock).acquire(workspace.workspaceId());
    mutationOrder.verify(permissionChecker).requirePermission(
        workspace.workspaceId(),
        WORKSPACE_OWNERSHIP_TRANSFER
    );

    assertThat(result)
        .returns(target.membershipId(), WorkspaceMemberView::membershipId)
        .returns(AccountRole.OWNER, WorkspaceMemberView::role)
        .satisfies(_ -> assertThat(membershipCaptor.getAllValues())
            .extracting(value -> value.aggregate().membershipId(), value -> value.aggregate().role())
            .containsExactly(
                tuple(target.membershipId(), AccountRole.OWNER),
                tuple(currentOwner.membershipId(), AccountRole.ADMIN)
            ));
    final var newOwnerCaptor = ArgumentCaptor.forClass(WorkspaceMembership.class);
    verify(auditTrail).recordOwnershipTransferred(
        eq(currentOwnerUser.userId()),
        eq(currentOwner),
        newOwnerCaptor.capture()
    );
    assertThat(newOwnerCaptor.getValue())
        .returns(target.membershipId(), WorkspaceMembership::membershipId)
        .returns(AccountRole.OWNER, WorkspaceMembership::role);
  }
}
