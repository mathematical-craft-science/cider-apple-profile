package org.craft.mcs.cap.account.context.application.usecase;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.craft.mcs.cap.core.account.context.application.contract.ResolvedWorkspaceContext;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.context.application.usecase.CurrentWorkspaceContextResolver;
import org.craft.mcs.cap.core.account.workspace.application.contract.WorkspaceSettingsRevision;
import org.craft.mcs.cap.core.account.workspace.application.contract.WorkspaceSettingsView;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceView;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.core.account.workspace.application.port.out.WorkspaceSettingsState;
import org.craft.mcs.cap.core.account.context.application.usecase.WorkspaceSettingsQueryService;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis.MALIC_ACID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WorkspaceSettingsQueryServiceTest {

  @Test
  void readsPersistedSettingsForCurrentWorkspaceContext() {
    final var contextResolver = mock(CurrentWorkspaceContextResolver.class);
    final var settingsRepository = mock(WorkspaceSettingsRepository.class);
    final var context = mock(ResolvedWorkspaceContext.class);
    final var membership = mock(WorkspaceMembershipView.class);
    final var workspace = mock(WorkspaceView.class);
    final var workspaceId = WorkspaceId.newId();
    final var settings = new WorkspaceSettings(workspaceId, Duration.ofDays(21), MALIC_ACID);
    when(contextResolver.resolve()).thenReturn(context);
    when(context.activeMembership()).thenReturn(membership);
    when(membership.workspace()).thenReturn(workspace);
    when(workspace.workspaceId()).thenReturn(workspaceId);
    when(settingsRepository.findByWorkspaceId(workspaceId)).thenReturn(Optional.of(state(settings)));
    final var service = new WorkspaceSettingsQueryService(
        contextResolver,
        settingsRepository
    );

    assertThat(service.readCurrent())
        .returns(workspaceId, WorkspaceSettingsView::workspaceId)
        .returns(Duration.ofDays(21), WorkspaceSettingsView::measurementStaleAfter)
        .returns(MALIC_ACID, WorkspaceSettingsView::defaultAcidityBasis);
  }

  @Test
  void resolvesPersistedSettingsByWorkspaceScope() {
    final var contextResolver = mock(CurrentWorkspaceContextResolver.class);
    final var settingsRepository = mock(WorkspaceSettingsRepository.class);
    final var workspaceId = WorkspaceId.newId();
    final var settings = new WorkspaceSettings(workspaceId, Duration.ofDays(10), MALIC_ACID);
    when(settingsRepository.findByWorkspaceId(workspaceId)).thenReturn(Optional.of(state(settings)));
    final var service = new WorkspaceSettingsQueryService(
        contextResolver,
        settingsRepository
    );

    assertThat(service.readByWorkspaceId(workspaceId))
        .returns(workspaceId, WorkspaceSettingsView::workspaceId)
        .returns(Duration.ofDays(10), WorkspaceSettingsView::measurementStaleAfter);
    verifyNoInteractions(contextResolver);
  }

  private static @NonNull WorkspaceSettingsState state(@NonNull WorkspaceSettings settings) {
    return new WorkspaceSettingsState(
        settings,
        WorkspaceSettingsRevision.of(0),
        Instant.EPOCH,
        Instant.EPOCH
    );
  }
}
