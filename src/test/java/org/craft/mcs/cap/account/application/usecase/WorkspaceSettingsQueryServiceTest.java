package org.craft.mcs.cap.account.application.usecase;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.core.account.application.model.ResolvedWorkspaceContext;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceSettingsView;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceView;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceRepository;
import org.craft.mcs.cap.core.account.application.port.out.WorkspaceSettingsRepository;
import org.craft.mcs.cap.core.account.application.usecase.CurrentWorkspaceContextResolver;
import org.craft.mcs.cap.core.account.application.usecase.WorkspaceSettingsQueryService;
import org.craft.mcs.cap.core.account.domain.model.Workspace;
import org.craft.mcs.cap.core.account.domain.model.WorkspaceSettings;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
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
    final var workspaceRepository = mock(WorkspaceRepository.class);
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
    when(settingsRepository.findByWorkspaceId(workspaceId)).thenReturn(Optional.of(settings));
    final var service = new WorkspaceSettingsQueryService(
        contextResolver,
        workspaceRepository,
        settingsRepository
    );

    assertThat(service.readCurrent())
        .returns(workspaceId, WorkspaceSettingsView::workspaceId)
        .returns(Duration.ofDays(21), WorkspaceSettingsView::measurementStaleAfter)
        .returns(MALIC_ACID, WorkspaceSettingsView::defaultAcidityBasis);
    verifyNoInteractions(workspaceRepository);
  }

  @Test
  void resolvesPersistedSettingsByCiderMakerScope() {
    final var contextResolver = mock(CurrentWorkspaceContextResolver.class);
    final var workspaceRepository = mock(WorkspaceRepository.class);
    final var settingsRepository = mock(WorkspaceSettingsRepository.class);
    final var ciderMakerId = CiderMakerId.of(UUID.randomUUID());
    final var workspace = mock(Workspace.class);
    final var workspaceId = WorkspaceId.newId();
    final var settings = new WorkspaceSettings(workspaceId, Duration.ofDays(10), MALIC_ACID);
    when(workspaceRepository.findByCiderMakerId(ciderMakerId)).thenReturn(Optional.of(workspace));
    when(workspace.workspaceId()).thenReturn(workspaceId);
    when(settingsRepository.findByWorkspaceId(workspaceId)).thenReturn(Optional.of(settings));
    final var service = new WorkspaceSettingsQueryService(
        contextResolver,
        workspaceRepository,
        settingsRepository
    );

    assertThat(service.readByCiderMakerId(ciderMakerId))
        .returns(workspaceId, WorkspaceSettingsView::workspaceId)
        .returns(Duration.ofDays(10), WorkspaceSettingsView::measurementStaleAfter);
    verifyNoInteractions(contextResolver);
  }
}
