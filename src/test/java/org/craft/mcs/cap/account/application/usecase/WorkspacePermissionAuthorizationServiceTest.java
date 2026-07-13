package org.craft.mcs.cap.account.application.usecase;

import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.core.account.application.AccountAccessDeniedException;
import org.craft.mcs.cap.core.account.application.model.AuthenticatedActor;
import org.craft.mcs.cap.core.account.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.application.port.out.CurrentActorProvider;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.application.usecase.WorkspacePermissionAuthorizationService;
import org.craft.mcs.cap.core.account.domain.model.AccountPermission;
import org.craft.mcs.cap.core.account.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.domain.model.Workspace;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceType;
import org.craft.mcs.cap.core.account.domain.model.value.DisplayName;
import org.craft.mcs.cap.core.account.domain.model.value.EmailAddress;
import org.craft.mcs.cap.core.account.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.domain.model.value.IdentityProviderCode;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
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
        fixture.workspace().ciderMakerId(),
        AccountPermission.FEEDSTOCK_READ
    )).doesNotThrowAnyException();

    verify(fixture.userRepository(), never()).save(any());
    verify(fixture.workspaceRepository(), never()).save(any());
    verify(fixture.membershipRepository(), never()).save(any());
  }

  @Test
  void deniesPermissionMissingFromMembershipRole() {
    final var fixture = fixture(AccountRole.LAB_CONTRIBUTOR);

    assertThatThrownBy(() -> fixture.service().requirePermission(
        fixture.workspace().ciderMakerId(),
        AccountPermission.PRODUCTION_WRITE
    )).isInstanceOf(AccountAccessDeniedException.class)
        .hasMessageContaining(fixture.workspace().ciderMakerId().value().toString());
  }

  @Test
  void deniesAnotherWorkspaceScopeWithoutProvisioningIt() {
    final var fixture = fixture(AccountRole.OWNER);
    final var requestedScope = CiderMakerId.of(UUID.randomUUID());
    when(fixture.workspaceRepository().findByCiderMakerId(requestedScope)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> fixture.service().requirePermission(
        requestedScope,
        AccountPermission.PRODUCTION_WRITE
    )).isInstanceOf(AccountAccessDeniedException.class)
        .hasMessageContaining(requestedScope.value().toString());

    verify(fixture.userRepository(), never()).save(any());
    verify(fixture.workspaceRepository(), never()).save(any());
    verify(fixture.membershipRepository(), never()).save(any());
  }

  private static @NonNull Fixture fixture(@NonNull AccountRole role) {
    final var actorProvider = mock(CurrentActorProvider.class);
    final var userRepository = mock(AccountUserRepository.class);
    final var workspaceRepository = mock(WorkspaceRepository.class);
    final var membershipRepository = mock(WorkspaceMembershipRepository.class);
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
        membership.status()
    );

    when(actorProvider.currentActor()).thenReturn(Optional.of(actor));
    when(userRepository.findByExternalIdentity(actor.identityProvider(), actor.externalSubject()))
        .thenReturn(Optional.of(user));
    when(workspaceRepository.findByCiderMakerId(workspace.ciderMakerId()))
        .thenReturn(Optional.of(workspace));
    when(membershipRepository.findByWorkspaceIdAndUserId(workspace.workspaceId(), user.userId()))
        .thenReturn(Optional.of(membershipWithRole));

    return new Fixture(
        new WorkspacePermissionAuthorizationService(
            actorProvider,
            userRepository,
            workspaceRepository,
            membershipRepository
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
