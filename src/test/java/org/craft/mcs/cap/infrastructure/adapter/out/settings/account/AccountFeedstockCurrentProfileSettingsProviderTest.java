package org.craft.mcs.cap.infrastructure.adapter.out.settings.account;

import java.time.Duration;
import java.util.UUID;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceSettingsReader;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceSettingsView;
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
    final var workspaceId = WorkspaceId.of(UUID.randomUUID());
    when(workspaceSettingsReader.readByWorkspaceId(workspaceId)).thenReturn(new WorkspaceSettingsView(
        workspaceId,
        Duration.ofDays(21),
        MALIC_ACID
    ));
    final var provider = new AccountFeedstockCurrentProfileSettingsProvider(workspaceSettingsReader);

    assertThat(provider.get(workspaceId))
        .returns(Duration.ofDays(21), FeedstockCurrentProfileSettings::measurementStaleAfter);
  }
}
