package org.craft.mcs.cap.infrastructure.adapter.out.settings.account;

import java.time.Duration;
import java.time.Instant;
import org.craft.mcs.cap.core.account.workspace.application.model.WorkspaceSettingsRevision;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceSettingsReader;
import org.craft.mcs.cap.core.account.workspace.application.model.WorkspaceSettingsView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSettings;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis.MALIC_ACID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountFeedstockCurrentProfileSettingsProviderTest {

  @Test
  void mapsAccountWorkspaceSettingsToFeedstockSettings() {
    final var workspaceSettingsReader = mock(WorkspaceSettingsReader.class);
    final var workspaceId = WorkspaceId.newId();
    when(workspaceSettingsReader.readByWorkspaceId(workspaceId)).thenReturn(new WorkspaceSettingsView(
        workspaceId,
        Duration.ofDays(21),
        MALIC_ACID,
        WorkspaceSettingsRevision.of(0),
        Instant.EPOCH,
        Instant.EPOCH
    ));
    final var provider = new AccountFeedstockCurrentProfileSettingsProvider(workspaceSettingsReader);

    assertThat(provider.get(workspaceId))
        .returns(Duration.ofDays(21), FeedstockCurrentProfileSettings::measurementStaleAfter);
  }
}
