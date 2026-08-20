package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.vessel;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.vessel.application.VesselUsageProjectionException;
import org.craft.mcs.cap.core.vessel.application.contract.VesselCurrentContentType;
import org.craft.mcs.cap.core.vessel.application.contract.VesselCurrentContentView;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcVesselCurrentContentQuery.class
})
class JdbcVesselCurrentContentQueryDataJdbcTest {

  @Autowired
  private JdbcVesselCurrentContentQuery query;

  @Autowired
  private JdbcTemplate jdbc;

  @Test
  void projectsSingleCurrentFeedstockAndProductionContents() {
    final var workspaceId = persistWorkspace("Usage projection cidery");
    final var feedstockVesselId = persistVessel(workspaceId, "Tank F-01", "500");
    final var productionVesselId = persistVessel(workspaceId, "Tank P-01", "1000");
    final var feedstockId = persistFeedstock(workspaceId, feedstockVesselId, "October juice", "125");
    final var productionId = persistProduction(workspaceId, productionVesselId, "Primary cider", "800");

    final var contents = query.findAllByWorkspace(workspaceId);

    assertThat(contents)
        .containsOnlyKeys(feedstockVesselId, productionVesselId)
        .extractingByKey(feedstockVesselId)
        .returns(VesselCurrentContentType.FEEDSTOCK_BATCH, VesselCurrentContentView::subjectType)
        .returns(feedstockId, VesselCurrentContentView::subjectId)
        .returns("October juice", content -> content.displayName().value())
        .returns(VolumeValue.ofLiters("125"), VesselCurrentContentView::volume);
    assertThat(contents)
        .extractingByKey(productionVesselId)
        .returns(VesselCurrentContentType.PRODUCTION_BATCH, VesselCurrentContentView::subjectType)
        .returns(productionId, VesselCurrentContentView::subjectId)
        .returns("Primary cider", content -> content.displayName().value())
        .returns(VolumeValue.ofLiters("800"), VesselCurrentContentView::volume);
  }

  @Test
  void returnsNoCurrentContentForEmptyVessel() {
    final var workspaceId = persistWorkspace("Empty projection cidery");
    final var vesselId = persistVessel(workspaceId, "Tank F-02", "500");

    assertThat(query.findByVesselId(vesselId)).isEmpty();
    assertThat(query.findAllByWorkspace(workspaceId)).isEmpty();
  }

  @Test
  void rejectsCrossContextOccupancyCorruption() {
    final var workspaceId = persistWorkspace("Corrupted occupancy cidery");
    final var vesselId = persistVessel(workspaceId, "Tank X-01", "500");
    persistFeedstock(workspaceId, vesselId, "Feedstock occupant", "100");
    persistProduction(workspaceId, vesselId, "Production occupant", "100");

    assertThatThrownBy(() -> query.findByVesselId(vesselId))
        .isInstanceOf(VesselUsageProjectionException.class)
        .hasMessageContaining("has multiple active material assignments");
  }

  @Test
  void databaseRejectsTwoActiveFeedstockBatchesInOneVessel() {
    final var workspaceId = persistWorkspace("Feedstock uniqueness cidery");
    final var vesselId = persistVessel(workspaceId, "Tank F-03", "500");
    persistFeedstock(workspaceId, vesselId, "First occupant", "100");

    assertThatThrownBy(() -> persistFeedstock(workspaceId, vesselId, "Second occupant", "100"))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("ux_feedstock_batches_active_vessel");
  }

  @Test
  void databaseRejectsTwoActiveProductionBatchesInOneVessel() {
    final var workspaceId = persistWorkspace("Production uniqueness cidery");
    final var vesselId = persistVessel(workspaceId, "Tank P-03", "500");
    persistProduction(workspaceId, vesselId, "First occupant", "100");

    assertThatThrownBy(() -> persistProduction(workspaceId, vesselId, "Second occupant", "100"))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("ux_production_batches_active_vessel");
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
      String capacity
  ) {
    final var vesselId = VesselId.newId();
    jdbc.update(
        """
            INSERT INTO vessels (
                vessel_id, workspace_id, name, nominal_capacity_liters, material,
                operational_status, version, created_at, updated_at
            ) VALUES (?, ?, ?, ?, 'STAINLESS_STEEL', 'OPERATIONAL', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        vesselId.value(),
        workspaceId.value(),
        name,
        new BigDecimal(capacity)
    );
    return vesselId;
  }

  private java.util.UUID persistFeedstock(
      WorkspaceId workspaceId,
      VesselId vesselId,
      String name,
      String volume
  ) {
    final var batchId = newUuid();
    jdbc.update(
        """
            INSERT INTO feedstock_batches (
                batch_id, workspace_id, name, vessel_id, type, season_starts_on, season_ends_on,
                lifecycle, available_volume_liters, current_material_state_id, version, created_at, updated_at
            ) VALUES (?, ?, ?, ?, 'JUICE', DATE '2026-01-01', DATE '2026-12-31',
                      'ACTIVE', ?, ?, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        batchId,
        workspaceId.value(),
        name,
        vesselId.value(),
        new BigDecimal(volume),
        MaterialStateId.newId().value()
    );
    return batchId;
  }

  private java.util.UUID persistProduction(
      WorkspaceId workspaceId,
      VesselId vesselId,
      String name,
      String volume
  ) {
    final var batchId = newUuid();
    jdbc.update(
        """
            INSERT INTO production_batches (
                batch_id, workspace_id, name, vessel_id, lifecycle, alcoholic_fermentation_state,
                current_volume_liters, current_material_state_id, initial_sugar_specific_gravity,
                initial_acidity_g_l_as_malic, version, created_at, updated_at
            ) VALUES (?, ?, ?, ?, 'ACTIVE', 'NOT_STARTED', ?, ?, 1.052, 6.2, 0,
                      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        batchId,
        workspaceId.value(),
        name,
        vesselId.value(),
        new BigDecimal(volume),
        MaterialStateId.newId().value()
    );
    return batchId;
  }
}
