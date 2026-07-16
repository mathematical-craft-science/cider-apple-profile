package org.craft.mcs.cap.account.context.application.usecase;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.account.shared.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.context.application.port.out.ActiveWorkspaceProvider;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.context.application.usecase.ActiveWorkspaceSelectionPolicy;
import org.craft.mcs.cap.core.account.context.application.usecase.CurrentAccountMembershipReader;
import org.craft.mcs.cap.core.account.context.application.usecase.CurrentWorkspaceContextResolver;
import org.craft.mcs.cap.core.account.context.application.usecase.WorkspacePermissionAuthorizationService;
import org.craft.mcs.cap.core.account.context.application.usecase.WorkspacePermissionChecker;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipStatus;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.user.application.model.AuthenticatedActor;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentActorProvider;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkspacePermissionAuthorizationServiceTest {

  @Test
  void authorizesPermissionFromPersistedActiveMembershipWithoutWrites() {
    final var fixture = fixture(AccountRole.LAB_CONTRIBUTOR);

    assertThatCode(() -> fixture.service().requirePermission(
        fixture.workspace().workspaceId(),
        WorkspacePermission.FEEDSTOCK_READ
    )).doesNotThrowAnyException();

    verify(fixture.userRepository(), never()).create(any());
    verify(fixture.userRepository(), never()).update(any());
    verify(fixture.workspaceRepository(), never()).create(any());
    verify(fixture.workspaceRepository(), never()).update(any());
    verify(fixture.membershipRepository(), never()).create(any());
    verify(fixture.membershipRepository(), never()).update(any());
  }

  @Test
  void deniesPermissionMissingFromMembershipRole() {
    final var fixture = fixture(AccountRole.LAB_CONTRIBUTOR);

    assertThatThrownBy(() -> fixture.service().requirePermission(
        fixture.workspace().workspaceId(),
        WorkspacePermission.PRODUCTION_WRITE
    )).isInstanceOf(AccountAccessDeniedException.class)
        .hasMessageContaining(fixture.workspace().workspaceId().value().toString());
  }

  @Test
  void deniesAnotherWorkspaceScopeWithoutProvisioningIt() {
    final var fixture = fixture(AccountRole.OWNER);
    final var requestedScope = WorkspaceId.newId();

    assertThatThrownBy(() -> fixture.service().requirePermission(
        requestedScope,
        WorkspacePermission.PRODUCTION_WRITE
    )).isInstanceOf(AccountAccessDeniedException.class)
        .hasMessageContaining(requestedScope.value().toString());

    verify(fixture.userRepository(), never()).create(any());
    verify(fixture.userRepository(), never()).update(any());
    verify(fixture.workspaceRepository(), never()).create(any());
    verify(fixture.workspaceRepository(), never()).update(any());
    verify(fixture.membershipRepository(), never()).create(any());
    verify(fixture.membershipRepository(), never()).update(any());
  }

  @Test
  void deniesBusinessAccessAfterMembershipWasSuspended() {
    final var fixture = fixture(AccountRole.OWNER, WorkspaceMembershipStatus.SUSPENDED);

    assertThatThrownBy(() -> fixture.service().requirePermission(
        fixture.workspace().workspaceId(),
        WorkspacePermission.FEEDSTOCK_READ
    )).isInstanceOf(AccountAccessDeniedException.class)
        .hasMessage("account has no active workspace membership");

    verify(fixture.userRepository(), never()).create(any());
    verify(fixture.userRepository(), never()).update(any());
    verify(fixture.workspaceRepository(), never()).create(any());
    verify(fixture.workspaceRepository(), never()).update(any());
    verify(fixture.membershipRepository(), never()).create(any());
    verify(fixture.membershipRepository(), never()).update(any());
  }

  private static @NonNull Fixture fixture(@NonNull AccountRole role) {
    return fixture(role, WorkspaceMembershipStatus.ACTIVE);
  }

  private static @NonNull Fixture fixture(
      @NonNull AccountRole role,
      @NonNull WorkspaceMembershipStatus status
  ) {
    final var actorProvider = mock(CurrentActorProvider.class);
    final var userRepository = mock(AccountUserRepository.class);
    final var workspaceRepository = mock(WorkspaceRepository.class);
    final var membershipRepository = mock(WorkspaceMembershipRepository.class);
    final var activeWorkspaceProvider = mock(ActiveWorkspaceProvider.class);
    final var actor = new AuthenticatedActor(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_123"),
        EmailAddress.of("technologist@example.com"),
        DisplayName.of("CAP Technologist"),
        null,
        null
    );
    final var user = AccountUser.create(
        actor.identityProvider(),
        actor.externalSubject(),
        actor.email(),
        actor.displayName(),
        actor.avatarUrl()
    );
    final var workspace = Workspace.createDefault(
        DisplayName.of("My cidery"),
        WorkspaceType.CIDERY,
        null,
        null
    );
    final var membership = WorkspaceMembership.createOwner(workspace.workspaceId(), user.userId());
    final var membershipWithRole = new WorkspaceMembership(
        membership.membershipId(),
        membership.workspaceId(),
        membership.userId(),
        role,
        status
    );

    when(actorProvider.currentActor()).thenReturn(Optional.of(actor));
    when(userRepository.findByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.of(user));
    when(membershipRepository.findAllByUserId(user.userId())).thenReturn(List.of(membershipWithRole));
    when(workspaceRepository.findById(workspace.workspaceId()))
        .thenReturn(Optional.of(workspace));
    when(activeWorkspaceProvider.selectedWorkspaceId()).thenReturn(Optional.of(workspace.workspaceId()));

    return new Fixture(
        new WorkspacePermissionAuthorizationService(
            new WorkspacePermissionChecker(new CurrentWorkspaceContextResolver(
                new CurrentAccountUserResolver(actorProvider, userRepository),
                new CurrentAccountMembershipReader(membershipRepository, workspaceRepository),
                activeWorkspaceProvider,
                new ActiveWorkspaceSelectionPolicy()
            ))
        ),
        userRepository,
        workspaceRepository,
        membershipRepository,
        workspace
    );
  }

  private record Fixture(
      @NonNull WorkspacePermissionAuthorizationService service,
      @NonNull AccountUserRepository userRepository,
      @NonNull WorkspaceRepository workspaceRepository,
      @NonNull WorkspaceMembershipRepository membershipRepository,
      @NonNull Workspace workspace
  ) {
  }
}
