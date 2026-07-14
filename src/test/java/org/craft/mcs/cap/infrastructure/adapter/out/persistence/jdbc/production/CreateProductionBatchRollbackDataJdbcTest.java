package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.production.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.application.port.out.ConsumedProductionSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionSourceDisplayName;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceRecorder;
import org.craft.mcs.cap.core.production.application.port.out.ProductionSourceInventory;
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

import static java.util.Objects.requireNonNull;
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
  private @NonNull CreateProductionBatchService service;

  @Autowired
  private @NonNull JdbcTemplate jdbcTemplate;

  @Test
  void rollsBackProductionBatchWhenTraceAppendFails() {
    final var workspaceId = WorkspaceId.of(UUID.randomUUID());
    jdbcTemplate.update(
        """
            INSERT INTO workspaces (workspace_id, name, type, status, version, created_at, updated_at)
            VALUES (?, 'Rollback workspace', 'CIDERY', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """,
        workspaceId.value()
    );
    final var sourceReference = ProductionSourceReference.feedstockBatch(UUID.randomUUID());
    insertFeedstockSource(workspaceId, sourceReference);
    final var command = command(workspaceId, sourceReference);

    assertThatThrownBy(() -> service.create(command))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("trace persistence failed")
        .satisfies(exception -> assertThat(rollbackState(sourceReference))
            .returns(0L, RollbackState::productionBatchCount)
            .returns(new BigDecimal("250.000"), RollbackState::availableFeedstockVolume));
  }

  private void insertFeedstockSource(
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceReference sourceReference
  ) {
    jdbcTemplate.update("""
        INSERT INTO feedstock_batches (
            batch_id, workspace_id, name, type, season_starts_on, season_ends_on,
            available_volume_liters, version, created_at, updated_at
        ) VALUES (?, ?, 'Late juice', 'JUICE', DATE '2026-01-01', DATE '2026-12-31',
                  250, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """, sourceReference.id(), workspaceId.value());
  }

  private @NonNull RollbackState rollbackState(@NonNull ProductionSourceReference sourceReference) {
    final var availableVolume = jdbcTemplate.queryForObject(
        "SELECT available_volume_liters FROM feedstock_batches WHERE batch_id = ?",
        BigDecimal.class,
        sourceReference.id()
    );
    return new RollbackState(
        countProductionBatches(),
        requireNonNull(availableVolume, "available feedstock volume cannot be null")
    );
  }

  private long countProductionBatches() {
    final var count = jdbcTemplate.queryForObject(
        "select count(*) from production_batches",
        Long.class
    );
    return count == null ? 0L : count; // NOSONAR
  }

  private static @NonNull CreateProductionBatchCommand command(
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceReference sourceReference
  ) {
    return new CreateProductionBatchCommand(
        workspaceId,
        ProductionBatchName.of("Rollback candidate"),
        ProductionBatchKind.CIDER,
        new ProductionInput(
            ProductionSourceComposition.of(List.of(new ProductionSourceComponent(
                sourceReference,
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
    ProductionSourceInventory productionSourceInventory(@NonNull JdbcTemplate jdbcTemplate) {
      return (workspaceId, source) -> {
        jdbcTemplate.update("""
            UPDATE feedstock_batches
            SET available_volume_liters = available_volume_liters - ?
            WHERE batch_id = ? AND workspace_id = ?
            """, source.volume().liters(), source.reference().id(), workspaceId.value());
        return new ConsumedProductionSource(
            source.reference(),
            ProductionSourceDisplayName.of("Late juice"),
            source.volume(),
            ChemicalProfileAxes.of(
                axis(ChemicalAxis.SUGAR, "1.050"),
                axis(ChemicalAxis.ACIDITY, "6.1"),
                axis(ChemicalAxis.TANNINITY, "1400")
            ),
            ConfidenceLevel.LAB_VERIFIED
        );
      };
    }

    @Bean
    Clock clock() {
      return Clock.fixed(Instant.parse("2026-05-18T10:15:30Z"), ZoneOffset.UTC);
    }

    @Bean
    ProductionAccessControl productionAccessControl() {
      return new ProductionAccessControl() {
        @Override
        public void requireReadAccess(@NonNull WorkspaceId workspaceId) {
        }

        @Override
        public void requireWriteAccess(@NonNull WorkspaceId workspaceId) {
        }
      };
    }

    private static @NonNull AxisValue axis(
        @NonNull ChemicalAxis axis,
        @NonNull String value
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

  private record RollbackState(
      long productionBatchCount,
      @NonNull BigDecimal availableFeedstockVolume
  ) {
  }

}
