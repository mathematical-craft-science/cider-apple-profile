package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.vessel;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.vessel.application.model.VesselView;
import org.craft.mcs.cap.core.vessel.domain.model.VesselOperationalStatus;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcVesselAvailabilityQuery.class
})
class JdbcVesselAvailabilityQueryDataJdbcTest {

  @Autowired
  private JdbcVesselAvailabilityQuery query;

  @Autowired
  private JdbcTemplate jdbc;

  @Test
  void findsOnlyOperationalEmptyWorkspaceVesselsWithOptionalCaseInsensitiveNameFilter() {
    final var workspaceId = persistWorkspace("Availability cidery");
    final var otherWorkspaceId = persistWorkspace("Other cidery");
    final var barrelId = persistVessel(
        workspaceId,
        "barrel B-01",
        VesselOperationalStatus.OPERATIONAL
    );
    final var matchingId = persistVessel(
        workspaceId,
        "Fermentation tank F-01",
        VesselOperationalStatus.OPERATIONAL
    );
    final var literalWildcardId = persistVessel(
        workspaceId,
        "Pilot % vessel",
        VesselOperationalStatus.OPERATIONAL
    );
    final var feedstockVesselId = persistVessel(
        workspaceId,
        "Fermentation tank F-02",
        VesselOperationalStatus.OPERATIONAL
    );
    final var productionVesselId = persistVessel(
        workspaceId,
        "Fermentation tank P-01",
        VesselOperationalStatus.OPERATIONAL
    );
    persistVessel(workspaceId, "Fermentation tank X-01", VesselOperationalStatus.OUT_OF_SERVICE);
    persistVessel(otherWorkspaceId, "Fermentation tank O-01", VesselOperationalStatus.OPERATIONAL);
    persistFeedstock(workspaceId, feedstockVesselId);
    persistProduction(workspaceId, productionVesselId);

    assertThat(query.findAvailable(workspaceId, new TextSearchCriteria("FERMENTATION")))
        .extracting(VesselView::vesselId)
        .containsExactly(matchingId);
    assertThat(query.findAvailable(workspaceId, new TextSearchCriteria("%")))
        .extracting(VesselView::vesselId)
        .containsExactly(literalWildcardId);
    assertThat(query.findAvailable(workspaceId, null))
        .extracting(VesselView::vesselId)
        .containsExactly(barrelId, matchingId, literalWildcardId);
  }

  private WorkspaceId persistWorkspace(String name) {
    final var workspaceId = WorkspaceId.newId();
    jdbc.update(
        """
            INSERT INTO workspaces (workspace_id, name, type, status, version, created_at, updated_at)
            VALUES (?, ?, 'CIDERY', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        workspaceId.value(),
        name
    );
    return workspaceId;
  }

  private VesselId persistVessel(
      WorkspaceId workspaceId,
      String name,
      VesselOperationalStatus status
  ) {
    final var vesselId = VesselId.newId();
    jdbc.update(
        """
            INSERT INTO vessels (
                vessel_id, workspace_id, name, nominal_capacity_liters, material,
                operational_status, version, created_at, updated_at
            ) VALUES (?, ?, ?, 500, 'STAINLESS_STEEL', ?, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        vesselId.value(),
        workspaceId.value(),
        name,
        status.name()
    );
    return vesselId;
  }

  private void persistFeedstock(
      WorkspaceId workspaceId,
      VesselId vesselId
  ) {
    jdbc.update(
        """
            INSERT INTO feedstock_batches (
                batch_id, workspace_id, name, vessel_id, type, season_starts_on, season_ends_on,
                lifecycle, available_volume_liters, current_material_state_id, version, created_at, updated_at
            ) VALUES (?, ?, 'Occupied feedstock', ?, 'JUICE', DATE '2026-01-01', DATE '2026-12-31',
                      'ACTIVE', ?, ?, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        newUuid(),
        workspaceId.value(),
        vesselId.value(),
        new BigDecimal("100"),
        MaterialStateId.newId().value()
    );
  }

  private void persistProduction(
      WorkspaceId workspaceId,
      VesselId vesselId
  ) {
    jdbc.update(
        """
            INSERT INTO production_batches (
                batch_id, workspace_id, name, vessel_id, lifecycle, alcoholic_fermentation_state,
                current_volume_liters, current_material_state_id, initial_sugar_specific_gravity,
                initial_acidity_g_l_as_malic, version, created_at, updated_at
            ) VALUES (?, ?, 'Occupied production', ?, 'ACTIVE', 'NOT_STARTED', ?, ?, 1.052, 6.2, 0,
                      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        newUuid(),
        workspaceId.value(),
        vesselId.value(),
        new BigDecimal("100"),
        MaterialStateId.newId().value()
    );
  }
}
