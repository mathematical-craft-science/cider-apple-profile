package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceRecorder;
import org.craft.mcs.cap.core.production.application.usecase.CreateProductionBatchService;
import org.craft.mcs.cap.core.production.domain.model.ProductionInput;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.platform.persistence.SpringTransactionHelper;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    CreateProductionBatchRollbackDataJdbcTest.TestSliceConfiguration.class,
    JdbcProductionBatchRepository.class,
    CreateProductionBatchService.class,
    SpringTransactionHelper.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class CreateProductionBatchRollbackDataJdbcTest {

  @Autowired
  private CreateProductionBatchService service;

  @Autowired
  private TestFeedstockBatchRepository feedstockBatchRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void rollsBackProductionBatchWhenTraceAppendFails() {
    final var ciderMakerId = CiderMakerId.of(UUID.randomUUID());
    final var feedstockBatch = feedstockBatch(ciderMakerId);
    feedstockBatchRepository.batch = feedstockBatch;

    assertThatThrownBy(() -> service.create(command(ciderMakerId, feedstockBatch)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("trace persistence failed");

    assertThat(countProductionBatches()).isZero();
  }

  private long countProductionBatches() {
    final var count = jdbcTemplate.queryForObject(
        "select count(*) from production_batches",
        Long.class
    );
    return count == null ? 0L : count; // NOSONAR
  }

  private static CreateProductionBatchCommand command(
      CiderMakerId ciderMakerId,
      FeedstockBatch feedstockBatch
  ) {
    return new CreateProductionBatchCommand(
        ciderMakerId,
        ProductionBatchName.of("Rollback candidate"),
        ProductionBatchKind.CIDER,
        new ProductionInput(
            ProductionSourceComposition.of(List.of(new ProductionSourceComponent(
                feedstockBatch.batchId(),
                VolumeValue.ofLiters("100")
            ))),
            new ProductionProfileSnapshot(
                SpecificGravityValue.of("1.052"),
                TitratableAcidityValue.of("5.8"),
                TanninityValue.of("1200")
            ),
            null
        ),
        null
    );
  }

  private static FeedstockBatch feedstockBatch(CiderMakerId ciderMakerId) {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        ciderMakerId,
        FeedstockBatchName.of("Late juice"),
        FeedstockType.APPLE_JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        VolumeValue.ofLiters("250")
    );
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class TestSliceConfiguration {

    @Bean
    TestFeedstockBatchRepository feedstockBatchRepository() {
      return new TestFeedstockBatchRepository();
    }

    @Bean
    ProductionBatchTraceRecorder productionBatchTraceRecorder() {
      return (productionBatch, sources, occurredAt, note) -> {
        throw new IllegalStateException("trace persistence failed");
      };
    }

    @Bean
    Clock clock() {
      return Clock.fixed(Instant.parse("2026-05-18T10:15:30Z"), ZoneOffset.UTC);
    }
  }

  static final class TestFeedstockBatchRepository implements FeedstockBatchRepository {

    private FeedstockBatch batch;

    @Override
    public @NonNull FeedstockBatch save(@NonNull FeedstockBatch feedstockBatch) {
      batch = feedstockBatch;
      return feedstockBatch;
    }

    @Override
    public @NonNull Optional<@NonNull FeedstockBatch> findById(@NonNull FeedstockBatchId batchId) {
      return batch != null && batch.batchId().equals(batchId)
          ? Optional.of(batch)
          : Optional.empty();
    }

    @Override
    public @NonNull List<@NonNull FeedstockBatch> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId) {
      return batch != null && batch.ciderMakerId().equals(ciderMakerId)
          ? List.of(batch)
          : List.of();
    }

    @Override
    public void delete(@NonNull FeedstockBatch feedstockBatch) {
      batch = null;
    }
  }
}
