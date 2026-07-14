package org.craft.mcs.cap.account.workspace.application.usecase;

import java.time.Duration;
import org.craft.mcs.cap.core.account.audit.application.usecase.WorkspaceAdministrativeAuditAppender;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeSubjectId;
import org.craft.mcs.cap.core.account.membership.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.membership.application.port.out.WorkspaceMembershipRepository;
import org.craft.mcs.cap.core.account.membership.application.usecase.WorkspacePermissionChecker;
import org.craft.mcs.cap.core.account.membership.domain.model.AccountRole;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.workspace.application.model.WorkspaceSettingsDefaults;
import org.craft.mcs.cap.core.account.workspace.application.port.in.CreateWorkspaceCommand;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceAdministrationLock;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.core.account.workspace.application.usecase.WorkspaceLifecycleService;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.account.support.DirectTransactionHelper.INSTANCE;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAction.WORKSPACE_CREATED;
import static org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeSubjectType.WORKSPACE;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembershipStatus.ACTIVE;
import static org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis.MALIC_ACID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class WorkspaceLifecycleServiceTest {

  private static @NonNull AccountUser accountUser() {
    return AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_owner"),
        EmailAddress.of("owner@example.com"),
        DisplayName.of("Owner"),
        null
    );
  }

  @Test
  void createsWorkspaceSettingsOwnerMembershipAndAuditAtomically() {
    final var userResolver = mock(CurrentAccountUserResolver.class);
    final var permissionChecker = mock(WorkspacePermissionChecker.class);
    final var workspaceRepository = mock(WorkspaceRepository.class);
    final var settingsRepository = mock(WorkspaceSettingsRepository.class);
    final var membershipRepository = mock(WorkspaceMembershipRepository.class);
    final var administrationLock = mock(WorkspaceAdministrationLock.class);
    final var auditAppender = mock(WorkspaceAdministrativeAuditAppender.class);
    final var user = accountUser();
    final var command = new CreateWorkspaceCommand(
        DisplayName.of("North Orchard"),
        WorkspaceType.CIDERY
    );

    when(userResolver.resolve()).thenReturn(user);
    when(workspaceRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0, Workspace.class));
    when(membershipRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0, WorkspaceMembership.class));

    final var service = new WorkspaceLifecycleService(
        userResolver,
        permissionChecker,
        workspaceRepository,
        settingsRepository,
        membershipRepository,
        administrationLock,
        auditAppender,
        new WorkspaceSettingsDefaults(Duration.ofDays(14), MALIC_ACID),
        INSTANCE
    );

    final var result = service.create(command);

    assertThat(result)
        .returns(AccountRole.OWNER, WorkspaceMembershipView::role)
        .returns(command.name(), membership -> membership.workspace().name())
        .returns(command.type(), membership -> membership.workspace().type())
        .returns(ACTIVE, WorkspaceMembershipView::status);
    verify(settingsRepository).create(new WorkspaceSettings(
        result.workspace().workspaceId(),
        Duration.ofDays(14),
        MALIC_ACID
    ));
    verify(auditAppender).append(
        eq(result.workspace().workspaceId()),
        eq(user.userId()),
        eq(WORKSPACE_CREATED),
        eq(WORKSPACE),
        any(WorkspaceAdministrativeSubjectId.class),
        isNull(),
        any(WorkspaceAdministrativeAuditValue.class)
    );
  }
}
