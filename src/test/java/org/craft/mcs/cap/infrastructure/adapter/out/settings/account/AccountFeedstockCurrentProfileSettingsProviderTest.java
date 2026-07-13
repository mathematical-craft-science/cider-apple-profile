package org.craft.mcs.cap.infrastructure.adapter.out.settings.account;

import java.time.Duration;
import java.util.UUID;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceSettingsReader;
import org.craft.mcs.cap.core.account.application.port.in.WorkspaceSettingsView;
import org.craft.mcs.cap.core.account.domain.model.value.WorkspaceId;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSettings;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis.MALIC_ACID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class AccountFeedstockCurrentProfileSettingsProviderTest {

  @Test
  void mapsAccountWorkspaceSettingsToFeedstockSettings() {
    final var workspaceSettingsReader = mock(WorkspaceSettingsReader.class);
    final var ciderMakerId = CiderMakerId.of(UUID.randomUUID());
    when(workspaceSettingsReader.readByCiderMakerId(ciderMakerId)).thenReturn(new WorkspaceSettingsView(
        WorkspaceId.newId(),
        Duration.ofDays(21),
        MALIC_ACID
    ));
    final var provider = new AccountFeedstockCurrentProfileSettingsProvider(workspaceSettingsReader);

    assertThat(provider.get(ciderMakerId))
        .returns(Duration.ofDays(21), FeedstockCurrentProfileSettings::measurementStaleAfter);
  }
}
