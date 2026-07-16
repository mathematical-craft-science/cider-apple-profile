package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production;

import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.core.production.application.model.ProductionBatchView;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchQueryRepository;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchStatus;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
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
        ProductionBatchName.of("Updated production batch"),
        saved.aggregate().kind(),
        ProductionBatchStatus.PLANNED,
        saved.aggregate().volume(),
        saved.aggregate().profile(),
        "Updated note"
    );

    final var result = repository.update(saved.withAggregate(updated)).aggregate();

    assertThat(springDataRepository.count()).isOne();
    assertThat(result)
        .returns(ProductionBatchName.of("Updated production batch"), ProductionBatch::name)
        .returns("Updated note", ProductionBatch::note);
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
  }

  @Test
  void findsProductionBatchById() {
    final var saved = repository.create(productionBatch());

    final var found = queryRepository.findById(saved.batchId());

    assertThat(found).get()
        .extracting(ProductionBatchView::batchId)
        .isEqualTo(saved.batchId());
    assertThat(found.orElseThrow().profile().yeastAssimilableNitrogen())
        .isNotNull()
        .extracting(YeastAssimilableNitrogenValue::asBigDecimal)
        .satisfies(value -> assertThat(value).isEqualByComparingTo("180"));
  }

  private static ProductionBatch renamed(
      ProductionBatch batch,
      String name
  ) {
    return ProductionBatch.restore(
        batch.batchId(),
        batch.workspaceId(),
        ProductionBatchName.of(name),
        batch.kind(),
        batch.status(),
        batch.volume(),
        batch.profile(),
        batch.note()
    );
  }

  @Test
  void listsProductionBatchesByWorkspace() {
    final var workspaceId = persistWorkspace("Primary workspace");
      final var first = repository.create(productionBatch(workspaceId, "First production batch"));
      final var second = repository.create(productionBatch(workspaceId, "Second production batch"));
      repository.create(productionBatch(persistWorkspace("Other workspace"), "Other production batch"));

    final var found = queryRepository.findAllByWorkspaceId(workspaceId);

    assertThat(found)
        .extracting(ProductionBatchView::batchId)
        .containsExactlyInAnyOrder(second.batchId(), first.batchId());
  }

  private ProductionBatch productionBatch() {
    return productionBatch(persistWorkspace("Initial workspace"), "Initial production batch");
  }

  private static ProductionBatch productionBatch(
      WorkspaceId workspaceId,
      String name
  ) {
    return ProductionBatch.create(
        ProductionBatchId.newId(),
        workspaceId,
        ProductionBatchName.of(name),
        ProductionBatchKind.CIDER,
        VolumeValue.ofLiters("100"),
        new ProductionProfileSnapshot(
            SpecificGravityValue.of("1.052"),
            TitratableAcidityValue.of("5.8"),
            TanninityValue.of("1200"),
            YeastAssimilableNitrogenValue.of("180")
        ),
        null
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
