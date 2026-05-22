package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production;

import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchStatus;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

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

  @Test
  void savesExistingProductionBatchAsUpdate() {
    final var productionBatch = productionBatch();
    final var saved = repository.save(productionBatch);
    final var updated = ProductionBatch.restore(
        saved.batchId(),
        saved.ciderMakerId(),
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
    assertThat(result.name().value()).isEqualTo("Updated production batch");
    assertThat(result.note()).isEqualTo("Updated note");
  }

  private static ProductionBatch productionBatch() {
    return ProductionBatch.create(
        ProductionBatchId.newId(),
        CiderMakerId.of(UUID.randomUUID()),
        ProductionBatchName.of("Initial production batch"),
        ProductionBatchKind.CIDER,
        VolumeValue.ofLiters("100"),
        new ProductionProfileSnapshot(
            SpecificGravityValue.of("1.052"),
            TitratableAcidityValue.of("5.8"),
            TanninityValue.of("1200")
        ),
        null,
        null
    );
  }
}
