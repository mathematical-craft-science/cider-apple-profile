package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production;

import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.core.production.application.model.ProductionBatchView;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchQueryRepository;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.value.AlcoholicFermentationState;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.testsupport.VesselTestFixtures;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcProductionBatchRepository.class,
    JdbcProductionBatchQueryRepository.class
})
class JdbcProductionBatchPersistenceDataJdbcTest {

  @Autowired
  private SpringDataProductionBatchJdbcRepository springDataRepository;

  @Autowired
  private JdbcProductionBatchRepository repository;

  @Autowired
  private ProductionBatchQueryRepository queryRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void savesExistingProductionBatchAsUpdate() {
    final var productionBatch = productionBatch();
    final var created = repository.create(productionBatch);
    final var saved = repository.findVersionedById(created.batchId()).orElseThrow();
    final var updated = ProductionBatch.restore(
        saved.aggregate().batchId(),
        saved.aggregate().workspaceId(),
        BatchName.of("Updated production batch"),
        saved.aggregate().vesselId(),
        BatchLifecycle.ACTIVE,
        AlcoholicFermentationState.NOT_STARTED,
        saved.aggregate().currentVolume(),
        saved.aggregate().currentMaterialStateId(),
        saved.aggregate().initialProfileSnapshot(),
        "Updated note"
    );

    final var result = repository.update(saved.withAggregate(updated)).aggregate();

    assertThat(springDataRepository.count()).isOne();
    assertThat(result)
        .returns(BatchName.of("Updated production batch"), ProductionBatch::name)
        .returns("Updated note", ProductionBatch::note);
  }

  @Test
  void persistsCorrectedInitialVesselAssignmentWithoutChangingMaterialState() {
    final var workspace = persistWorkspace("Vessel correction workspace");
    final var targetVesselId = VesselId.newId();
    VesselTestFixtures.persist(jdbcTemplate, workspace.workspaceId(), targetVesselId, "Tank F-04");
    final var created = repository.create(productionBatch(workspace, "Vessel correction batch"));
    final var state = repository.findVersionedById(created.batchId()).orElseThrow();
    final var materialStateId = state.aggregate().currentMaterialStateId();

    state.aggregate().correctInitialVesselAssignment(targetVesselId);
    repository.update(state);

    assertThat(repository.findVersionedById(created.batchId()))
        .get()
        .extracting(VersionedAggregate::aggregate)
        .returns(targetVesselId, ProductionBatch::vesselId)
        .returns(materialStateId, ProductionBatch::currentMaterialStateId);
  }

  @Test
  void rejectsProductionBatchStateLoadedBeforeAnotherUpdate() {
    final var created = repository.create(productionBatch());
    final var first = repository.findVersionedById(created.batchId()).orElseThrow();
    final var stale = repository.findVersionedById(created.batchId()).orElseThrow();
    final var firstUpdate = renamed(first.aggregate(), "First update");
    final var staleUpdate = renamed(stale.aggregate(), "Stale update");

    repository.update(first.withAggregate(firstUpdate));

    assertThatThrownBy(() -> repository.update(stale.withAggregate(staleUpdate)))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessage("resource was modified concurrently: production batch");
    assertThat(repository.findVersionedById(created.batchId()))
        .get()
        .extracting(VersionedAggregate::aggregate)
        .extracting(ProductionBatch::name)
        .isEqualTo(BatchName.of("First update"));
  }

  @Test
  void findsProductionBatchById() {
    final var saved = repository.create(productionBatch());

    final var found = queryRepository.findById(saved.batchId());

    assertThat(found).get()
        .extracting(ProductionBatchView::batchId)
        .isEqualTo(saved.batchId());
    assertThat(found.orElseThrow().initialProfileSnapshot().yeastAssimilableNitrogen())
        .isNotNull()
        .extracting(YeastAssimilableNitrogenValue::asBigDecimal)
        .satisfies(value -> assertThat(value).isEqualByComparingTo("180"));
  }

  @Test
  void locksProductionBatchForMeasurementLifecycleValidation() {
    final var saved = repository.create(productionBatch());

    assertThat(repository.lockForMeasurementChange(saved.batchId()))
        .get()
        .returns(saved.batchId(), ProductionBatch::batchId)
        .returns(BatchLifecycle.ACTIVE, ProductionBatch::lifecycle);
  }

  private static ProductionBatch renamed(
      ProductionBatch batch,
      String name
  ) {
    return ProductionBatch.restore(
        batch.batchId(),
        batch.workspaceId(),
        BatchName.of(name),
        batch.vesselId(),
        batch.lifecycle(),
        batch.alcoholicFermentationState(),
        batch.currentVolume(),
        batch.currentMaterialStateId(),
        batch.initialProfileSnapshot(),
        batch.note()
    );
  }

  @Test
  void listsProductionBatchesByWorkspace() {
    final var primaryWorkspace = persistWorkspace("Primary workspace");
    final var first = repository.create(productionBatch(primaryWorkspace, "First production batch"));
    final var secondVesselId = VesselId.newId();
    VesselTestFixtures.persist(jdbcTemplate, primaryWorkspace.workspaceId(), secondVesselId, "Tank F-04");
    final var second = repository.create(productionBatch(
        new WorkspaceFixture(primaryWorkspace.workspaceId(), secondVesselId),
        "Second production batch"
    ));
    repository.create(productionBatch(persistWorkspace("Other workspace"), "Other production batch"));

    final var found = queryRepository.findAllByWorkspaceId(primaryWorkspace.workspaceId());

    assertThat(found)
        .extracting(ProductionBatchView::batchId)
        .containsExactlyInAnyOrder(second.batchId(), first.batchId());
  }

  private ProductionBatch productionBatch() {
    return productionBatch(persistWorkspace("Initial workspace"), "Initial production batch");
  }

  private static ProductionBatch productionBatch(
      WorkspaceFixture workspace,
      String name
  ) {
    return ProductionBatch.create(
        ProductionBatchId.newId(),
        workspace.workspaceId(),
        BatchName.of(name),
        workspace.vesselId(),
        VolumeValue.ofLiters("100"),
        new ProductionProfileSnapshot(
            SpecificGravityValue.of("1.052"),
            TitratableAcidityValue.of("5.8"),
            TanninValue.of("1200"),
            YeastAssimilableNitrogenValue.of("180")
        ),
        null
    );
  }

  private WorkspaceFixture persistWorkspace(String name) {
    final var workspaceId = WorkspaceId.newId();
    final var vesselId = VesselId.newId();
    jdbcTemplate.update(
        """
            INSERT INTO workspaces (workspace_id, name, type, status, version, created_at, updated_at)
            VALUES (?, ?, 'CIDERY', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        workspaceId.value(),
        name
    );
    VesselTestFixtures.persist(jdbcTemplate, workspaceId, vesselId, "Tank F-03");
    return new WorkspaceFixture(workspaceId, vesselId);
  }

  private record WorkspaceFixture(
      WorkspaceId workspaceId,
      VesselId vesselId
  ) {
  }
}
