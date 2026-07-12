package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchFeedstockTraceSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceRecorder;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionSourceConsumptionPort;
import org.craft.mcs.cap.core.production.application.usecase.CreateProductionBatchService;
import org.craft.mcs.cap.core.production.domain.model.ProductionInput;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.platform.persistence.SpringTransactionHelper;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
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
  private JdbcTemplate jdbcTemplate;

  @Test
  void rollsBackProductionBatchWhenTraceAppendFails() {
    final var ciderMakerId = CiderMakerId.of(UUID.randomUUID());
    final var sourceId = UUID.randomUUID();

    assertThatThrownBy(() -> service.create(command(ciderMakerId, sourceId)))
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
      UUID sourceId
  ) {
    return new CreateProductionBatchCommand(
        ciderMakerId,
        ProductionBatchName.of("Rollback candidate"),
        ProductionBatchKind.CIDER,
        new ProductionInput(
            ProductionSourceComposition.of(List.of(new ProductionSourceComponent(
                ProductionSourceReference.feedstockBatch(sourceId),
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

  @TestConfiguration(proxyBeanMethods = false)
  static class TestSliceConfiguration {

    @Bean
    ProductionBatchTraceRecorder productionBatchTraceRecorder() {
      return (productionBatch, sources, occurredAt, note) -> {
        throw new IllegalStateException("trace persistence failed");
      };
    }

    @Bean
    ProductionSourceConsumptionPort productionSourceConsumptionPort() {
      return (ciderMakerId, sources) -> sources.stream()
          .map(source -> new ProductionBatchFeedstockTraceSource(
              FeedstockBatchId.of(source.reference().id()),
              "Late juice",
              source.volume(),
              ChemicalProfileAxes.of(
                  axis(ChemicalAxis.SUGAR, "1.050"),
                  axis(ChemicalAxis.ACIDITY, "6.1"),
                  axis(ChemicalAxis.TANNINITY, "1400")
              ),
              ConfidenceLevel.LAB_VERIFIED
          ))
          .map(ProductionBatchTraceSource.class::cast)
          .toList();
    }

    @Bean
    Clock clock() {
      return Clock.fixed(Instant.parse("2026-05-18T10:15:30Z"), ZoneOffset.UTC);
    }

    private static AxisValue axis(
        ChemicalAxis axis,
        String value
    ) {
      return new AxisValue(
          axis,
          new BigDecimal(value),
          MeasurementValueKind.EXACT,
          null,
          null,
          null,
          null
      );
    }
  }
}
