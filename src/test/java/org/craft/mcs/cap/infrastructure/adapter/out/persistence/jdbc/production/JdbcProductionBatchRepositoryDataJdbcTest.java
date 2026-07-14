package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production;

import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
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


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcProductionBatchRepository.class
})
class JdbcProductionBatchRepositoryDataJdbcTest {

  @Autowired
  private SpringDataProductionBatchJdbcRepository springDataRepository;

  @Autowired
  private JdbcProductionBatchRepository repository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void savesExistingProductionBatchAsUpdate() {
    final var productionBatch = productionBatch();
    final var saved = repository.save(productionBatch);
    final var updated = ProductionBatch.restore(
        saved.batchId(),
        saved.workspaceId(),
        ProductionBatchName.of("Updated production batch"),
        saved.kind(),
        ProductionBatchStatus.PLANNED,
        saved.volume(),
        saved.profile(),
        saved.targetDistance(),
        "Updated note"
    );

    final var result = repository.save(updated);

    assertThat(springDataRepository.count()).isOne();
    assertThat(result)
        .returns(ProductionBatchName.of("Updated production batch"), ProductionBatch::name)
        .returns("Updated note", ProductionBatch::note);
  }

  @Test
  void findsProductionBatchById() {
    final var saved = repository.save(productionBatch());

    final var found = repository.findById(saved.batchId());

    assertThat(found).get()
        .extracting(ProductionBatch::batchId)
        .isEqualTo(saved.batchId());
    assertThat(found.orElseThrow().profile().yeastAssimilableNitrogen())
        .isNotNull()
        .extracting(YeastAssimilableNitrogenValue::asBigDecimal)
        .satisfies(value -> assertThat(value).isEqualByComparingTo("180"));
  }

  @Test
  void listsProductionBatchesByWorkspace() {
    final var workspaceId = persistWorkspace("Primary workspace");
    final var first = repository.save(productionBatch(workspaceId, "First production batch"));
    final var second = repository.save(productionBatch(workspaceId, "Second production batch"));
    repository.save(productionBatch(persistWorkspace("Other workspace"), "Other production batch"));

    final var found = repository.findAllByWorkspaceId(workspaceId);

    assertThat(found)
        .extracting(ProductionBatch::batchId)
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
        null,
        null
    );
  }

  private WorkspaceId persistWorkspace(String name) {
    final var workspaceId = WorkspaceId.of(UUID.randomUUID());
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
