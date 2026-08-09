package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.vessel;

import java.util.Set;
import org.craft.mcs.cap.core.vessel.application.VesselNameConflictException;
import org.craft.mcs.cap.core.vessel.domain.model.Vessel;
import org.craft.mcs.cap.core.vessel.domain.model.VesselMaterial;
import org.craft.mcs.cap.core.vessel.domain.model.VesselOperationalStatus;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
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
    JdbcVesselRepository.class,
    JdbcVesselCatalogQuery.class
})
class JdbcVesselPersistenceDataJdbcTest {

  @Autowired
  private JdbcVesselRepository repository;

  @Autowired
  private JdbcVesselCatalogQuery catalogQuery;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void roundTripsAggregateAndCatalogProjection() {
    final var workspaceId = persistWorkspace("Primary cidery");
    final var created = repository.create(vessel(workspaceId, "Tank F-03"));
    final var vessel = created.aggregate();

    final var loaded = repository.findVersionedById(vessel.vesselId()).orElseThrow();
    final var catalogEntry = catalogQuery.findById(vessel.vesselId()).orElseThrow();

    assertThat(loaded.aggregate())
        .returns(vessel.vesselId(), Vessel::vesselId)
        .returns(workspaceId, Vessel::workspaceId)
        .returns("Tank F-03", Vessel::name)
        .returns(VolumeValue.ofLiters("500"), Vessel::nominalCapacity)
        .returns(VesselMaterial.STAINLESS_STEEL, Vessel::material)
        .returns(VesselOperationalStatus.OPERATIONAL, Vessel::operationalStatus);
    assertThat(loaded.revision()).isEqualTo(created.revision());
    assertThat(catalogEntry.revision()).isEqualTo(created.revision());
  }

  @Test
  void rejectsStaleAggregateRevision() {
    final var workspaceId = persistWorkspace("Revision cidery");
    final var created = repository.create(vessel(workspaceId, "Tank F-03"));
    final var first = repository.findVersionedById(created.aggregate().vesselId()).orElseThrow();
    final var stale = repository.findVersionedById(created.aggregate().vesselId()).orElseThrow();

    first.aggregate().updateCharacteristics(
        "Tank F-03 renamed",
        VolumeValue.ofLiters("600"),
        VesselMaterial.STAINLESS_STEEL
    );
    repository.update(first);
    stale.aggregate().changeOperationalStatus(VesselOperationalStatus.OUT_OF_SERVICE);

    assertThatThrownBy(() -> repository.update(stale))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessage("resource was modified concurrently: vessel");
    assertThat(repository.findVersionedById(created.aggregate().vesselId()))
        .get()
        .extracting(VersionedAggregate::aggregate)
        .extracting(Vessel::name)
        .isEqualTo("Tank F-03 renamed");
  }

  @Test
  void translatesCaseInsensitiveWorkspaceNameConflict() {
    final var workspaceId = persistWorkspace("Name cidery");
    repository.create(vessel(workspaceId, "Tank F-03"));
    final var duplicate = vessel(workspaceId, "tank f-03");

    assertThatThrownBy(() -> repository.create(duplicate))
        .isInstanceOf(VesselNameConflictException.class)
        .hasMessageContaining("already contains vessel named tank f-03");
  }

  @Test
  void resolvesOnlyRequestedWorkspaceVesselReferences() {
    final var workspaceId = persistWorkspace("Catalog cidery");
    final var otherWorkspaceId = persistWorkspace("Other cidery");
    final var first = repository.create(vessel(workspaceId, "Tank F-03")).aggregate();
    final var second = repository.create(vessel(workspaceId, "Tank F-04")).aggregate();
    final var other = repository.create(vessel(otherWorkspaceId, "Other tank")).aggregate();

    final var references = catalogQuery.findReferences(
        workspaceId,
        Set.of(first.vesselId(), second.vesselId(), other.vesselId())
    );

    assertThat(references)
        .containsOnlyKeys(first.vesselId(), second.vesselId())
        .extractingByKey(first.vesselId())
        .returns("Tank F-03", VesselReferenceView::name);
  }

  @Test
  void rejectsFeedstockAssignmentToVesselOwnedByAnotherWorkspace() {
    final var vesselWorkspaceId = persistWorkspace("Vessel owner");
    final var batchWorkspaceId = persistWorkspace("Batch owner");
    final var vessel = repository.create(vessel(vesselWorkspaceId, "Tank F-03")).aggregate();

    assertThatThrownBy(() -> jdbcTemplate.update(
        """
            INSERT INTO feedstock_batches (
                batch_id, workspace_id, name, vessel_id, type, season_starts_on, season_ends_on,
                lifecycle, available_volume_liters, current_material_state_id, version, created_at, updated_at
            ) VALUES (?, ?, 'Cross-workspace feedstock', ?, 'JUICE', DATE '2026-01-01', DATE '2026-12-31',
                      'ACTIVE', 100, ?, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        newUuid(),
        batchWorkspaceId.value(),
        vessel.vesselId().value(),
        MaterialStateId.newId().value()
    ))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("fk_feedstock_batches_vessel");
  }

  @Test
  void rejectsProductionAssignmentToVesselOwnedByAnotherWorkspace() {
    final var vesselWorkspaceId = persistWorkspace("Production vessel owner");
    final var batchWorkspaceId = persistWorkspace("Production batch owner");
    final var vessel = repository.create(vessel(vesselWorkspaceId, "Tank P-01")).aggregate();

    assertThatThrownBy(() -> jdbcTemplate.update(
        """
            INSERT INTO production_batches (
                batch_id, workspace_id, name, vessel_id, lifecycle, alcoholic_fermentation_state,
                current_volume_liters, current_material_state_id, initial_sugar_specific_gravity,
                initial_acidity_g_l_as_malic, version, created_at, updated_at
            ) VALUES (?, ?, 'Cross-workspace production', ?, 'ACTIVE', 'NOT_STARTED',
                      100, ?, 1.052, 6.2, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        newUuid(),
        batchWorkspaceId.value(),
        vessel.vesselId().value(),
        MaterialStateId.newId().value()
    ))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("fk_production_batches_vessel");
  }

  private static Vessel vessel(
      WorkspaceId workspaceId,
      String name
  ) {
    return Vessel.create(
        VesselId.newId(),
        workspaceId,
        name,
        VolumeValue.ofLiters("500"),
        VesselMaterial.STAINLESS_STEEL
    );
  }

  private WorkspaceId persistWorkspace(String name) {
    final var workspaceId = WorkspaceId.newId();
    jdbcTemplate.update(
        """
            INSERT INTO workspaces (workspace_id, name, type, status, version, created_at, updated_at)
            VALUES (?, ?, 'CIDERY', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        workspaceId.value(),
        name
    );
    return workspaceId;
  }
}
