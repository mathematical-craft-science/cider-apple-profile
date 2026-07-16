package org.craft.mcs.cap.account.administration.application.usecase;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.craft.mcs.cap.core.account.context.application.model.ResolvedWorkspaceContext;
import org.craft.mcs.cap.core.account.context.application.usecase.WorkspacePermissionChecker;
import org.craft.mcs.cap.core.account.shared.domain.value.AccountUserId;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.workspace.application.model.WorkspaceSettingsRevision;
import org.craft.mcs.cap.core.account.workspace.application.port.in.UpdateWorkspaceSettingsCommand;
import org.craft.mcs.cap.core.account.workspace.application.model.WorkspaceSettingsView;
import org.craft.mcs.cap.core.account.shared.application.port.out.WorkspaceAdministrationLock;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsState;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceAuditTrail;
import org.craft.mcs.cap.core.account.administration.application.usecase.GetWorkspaceSettingsService;
import org.craft.mcs.cap.core.account.administration.application.usecase.UpdateWorkspaceSettingsService;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.account.support.DirectTransactionHelper.INSTANCE;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.WORKSPACE_SETTINGS_READ;
import static org.craft.mcs.cap.core.account.membership.domain.model.WorkspacePermission.WORKSPACE_SETTINGS_UPDATE;
import static org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis.MALIC_ACID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class WorkspaceSettingsManagementServiceTest {

  private static final Instant CREATED_AT = Instant.parse("2026-07-01T00:00:00Z");
  private static final Instant UPDATED_AT = Instant.parse("2026-07-14T00:00:00Z");

  @Test
  void readsWorkspaceSettingsAfterPermissionCheck() {
    final var permissionChecker = mock(WorkspacePermissionChecker.class);
    final var repository = mock(WorkspaceSettingsRepository.class);
    final var workspaceId = WorkspaceId.newId();
    final var state = state(workspaceId, Duration.ofDays(14), MALIC_ACID, 3, UPDATED_AT);
    when(repository.findByWorkspaceId(state.settings().workspaceId())).thenReturn(Optional.of(state));
    final var service = new GetWorkspaceSettingsService(permissionChecker, repository);

    assertThat(service.get(state.settings().workspaceId()))
        .returns(state.settings().workspaceId(), WorkspaceSettingsView::workspaceId)
        .returns(Duration.ofDays(14), WorkspaceSettingsView::measurementStaleAfter)
        .returns(MALIC_ACID, WorkspaceSettingsView::defaultAcidityBasis)
        .returns(WorkspaceSettingsRevision.of(3), WorkspaceSettingsView::revision)
        .returns(UPDATED_AT, WorkspaceSettingsView::updatedAt);
    verify(permissionChecker).requirePermission(state.settings().workspaceId(), WORKSPACE_SETTINGS_READ);
  }

  @Test
  void updatesOnlyProvidedSettingsAndPreservesExpectedRevision() {
    final var permissionChecker = mock(WorkspacePermissionChecker.class);
    final var repository = mock(WorkspaceSettingsRepository.class);
    final var administrationLock = mock(WorkspaceAdministrationLock.class);
    final var auditTrail = mock(WorkspaceAuditTrail.class);
    final var context = mock(ResolvedWorkspaceContext.class);
    final var user = mock(AccountUser.class);
    final var workspaceId = WorkspaceId.newId();
    final var current = state(workspaceId, Duration.ofDays(14), MALIC_ACID, 3, CREATED_AT);
    final var updated = state(workspaceId, Duration.ofDays(21), MALIC_ACID, 4, UPDATED_AT);
    final var expectedRevision = current.revision();
    final var command = new UpdateWorkspaceSettingsCommand(
        current.settings().workspaceId(),
        expectedRevision,
        Duration.ofDays(21),
        null
    );
    when(repository.findByWorkspaceId(command.workspaceId())).thenReturn(Optional.of(current));
    when(repository.update(any(), any())).thenReturn(Optional.of(updated));
    when(permissionChecker.requirePermission(command.workspaceId(), WORKSPACE_SETTINGS_UPDATE))
        .thenReturn(context);
    when(context.user()).thenReturn(user);
    final var actorUserId = AccountUserId.newId();
    when(user.userId()).thenReturn(actorUserId);
    final var service = new UpdateWorkspaceSettingsService(
        permissionChecker,
        repository,
        administrationLock,
        auditTrail,
        INSTANCE
    );

    assertThat(service.update(command))
        .returns(Duration.ofDays(21), WorkspaceSettingsView::measurementStaleAfter)
        .returns(MALIC_ACID, WorkspaceSettingsView::defaultAcidityBasis)
        .returns(WorkspaceSettingsRevision.of(4), WorkspaceSettingsView::revision)
        .returns(UPDATED_AT, WorkspaceSettingsView::updatedAt);
    final var mutationOrder = inOrder(administrationLock, permissionChecker);
    mutationOrder.verify(administrationLock).acquire(command.workspaceId());
    mutationOrder.verify(permissionChecker).requirePermission(
        command.workspaceId(),
        WORKSPACE_SETTINGS_UPDATE
    );
    final var settingsCaptor = ArgumentCaptor.forClass(WorkspaceSettings.class);
    verify(repository).update(settingsCaptor.capture(), eq(expectedRevision));
    assertThat(settingsCaptor.getValue())
        .returns(Duration.ofDays(21), WorkspaceSettings::measurementStaleAfter)
        .returns(MALIC_ACID, WorkspaceSettings::defaultAcidityBasis);
    verify(auditTrail).recordSettingsUpdated(actorUserId, current.settings(), updated.settings());
  }

  @Test
  void rejectsUpdateWithoutChangedSettings() {
    final var workspaceId = WorkspaceId.newId();
    final var revision = WorkspaceSettingsRevision.of(3);

    assertThatThrownBy(() -> new UpdateWorkspaceSettingsCommand(workspaceId, revision, null, null))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("workspace settings update must contain at least one value");
  }

  private static @NonNull WorkspaceSettingsState state(
      @NonNull WorkspaceId workspaceId,
      @NonNull Duration staleAfter,
      @NonNull TitratableAcidityBasis acidityBasis,
      long revision,
      @NonNull Instant updatedAt
  ) {
    return new WorkspaceSettingsState(
        new WorkspaceSettings(workspaceId, staleAfter, acidityBasis),
        WorkspaceSettingsRevision.of(revision),
        CREATED_AT,
        updatedAt
    );
  }

}
