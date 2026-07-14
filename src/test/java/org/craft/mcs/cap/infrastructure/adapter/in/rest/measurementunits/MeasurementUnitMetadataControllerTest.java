package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits;

import java.time.Duration;
import java.time.Instant;
import org.craft.mcs.cap.core.account.workspace.application.model.WorkspaceSettingsRevision;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceSettingsReader;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceSettingsView;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits.dto.MeasurementFreshnessMetadataResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits.dto.MeasurementUnitsMetadataResponse;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidityBasis.MALIC_ACID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MeasurementUnitMetadataControllerTest {

  @Test
  void returnsFreshnessThresholdFromCurrentWorkspaceSettings() {
    final var workspaceSettingsReader = mock(WorkspaceSettingsReader.class);
    when(workspaceSettingsReader.readCurrent()).thenReturn(new WorkspaceSettingsView(
        WorkspaceId.newId(),
        Duration.ofDays(21),
        MALIC_ACID,
        WorkspaceSettingsRevision.of(0),
        Instant.EPOCH,
        Instant.EPOCH
    ));
    final var controller = new MeasurementUnitMetadataController(workspaceSettingsReader);

    assertThat(controller.metadata().getBody())
        .isNotNull()
        .extracting(MeasurementUnitsMetadataResponse::measurementFreshness)
        .returns("PT504H", MeasurementFreshnessMetadataResponse::staleAfter);
  }
}
