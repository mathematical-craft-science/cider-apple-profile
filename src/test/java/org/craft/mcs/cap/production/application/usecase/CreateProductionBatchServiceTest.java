package org.craft.mcs.cap.production.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.production.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockVessel;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchFeedstockTraceSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceRecorder;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionFeedstockTraceSnapshot;
import org.craft.mcs.cap.core.production.application.usecase.CreateProductionBatchService;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.ProductionInput;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;


class CreateProductionBatchServiceTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-05-18T10:15:30Z"), ZoneOffset.UTC);

  @Test
  void createsProductionBatchAndRecordsTraceFactInSameTransaction() {
    final var transactionHelper = new StagingTransactionHelper();
    final var ciderMakerId = CiderMakerId.of(UUID.randomUUID());
    final var feedstockBatch = feedstockBatch(ciderMakerId, "Late juice", "250");
    final var feedstockRepository = new InMemoryFeedstockBatchRepository(feedstockBatch);
    final var productionRepository = new StagingProductionBatchRepository(transactionHelper);
    final var traceability = new CapturingTraceRecorder();
    final var service = new CreateProductionBatchService(
        feedstockRepository,
        productionRepository,
        traceability,
        batchId -> feedstockSnapshot(),
        mock(ProductionAccessControl.class),
        transactionHelper,
        CLOCK
    );

    final var created = service.create(command(ciderMakerId, feedstockBatch));

    assertThat(created.productionBatch()).isNotNull();
    assertThat(productionRepository.committed).hasSize(1);
    assertThat(traceability.recorded).isNotNull();
    assertThat(traceability.recorded.productionBatch()).isEqualTo(created.productionBatch());
    assertThat(traceability.recorded.sources()).singleElement()
        .satisfies(source -> {
          assertThat(source).isInstanceOf(ProductionBatchFeedstockTraceSource.class);
          assertThat(source.id()).isEqualTo(feedstockBatch.batchId().value().toString());
          assertThat(source.displayName()).isEqualTo("Late juice");
          assertThat(source.volume().liters()).isEqualByComparingTo("100");
          assertThat(source.profile().valueOf(ChemicalAxis.SUGAR))
              .get()
              .satisfies(value -> assertThat(value.value()).isEqualByComparingTo("1.050"));
          assertThat(source.confidence()).isEqualTo(ConfidenceLevel.LAB_VERIFIED);
        });
    assertThat(traceability.recorded.occurredAt()).isEqualTo(Instant.now(CLOCK));
    assertThat(traceability.recorded.note()).isEqualTo("accepted production input");
  }

  @Test
  void doesNotCommitProductionBatchWhenTraceStepCannotBeAppended() {
    final var transactionHelper = new StagingTransactionHelper();
    final var ciderMakerId = CiderMakerId.of(UUID.randomUUID());
    final var feedstockBatch = feedstockBatch(ciderMakerId, "Late juice", "250");
    final var productionRepository = new StagingProductionBatchRepository(transactionHelper);
    final var service = new CreateProductionBatchService(
        new InMemoryFeedstockBatchRepository(feedstockBatch),
        productionRepository,
        (productionBatch, sources, occurredAt, note) -> {
          throw new IllegalStateException("trace persistence failed");
        },
        batchId -> feedstockSnapshot(),
        mock(ProductionAccessControl.class),
        transactionHelper,
        CLOCK
    );

    assertThatThrownBy(() -> service.create(command(ciderMakerId, feedstockBatch)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("trace persistence failed");

    assertThat(productionRepository.committed).isEmpty();
  }

  private static CreateProductionBatchCommand command(
      CiderMakerId ciderMakerId,
      FeedstockBatch feedstockBatch
  ) {
    return new CreateProductionBatchCommand(
        ciderMakerId,
        ProductionBatchName.of("Classic cider path"),
        ProductionBatchKind.CIDER,
        new ProductionInput(
            ProductionSourceComposition.of(List.of(new ProductionSourceComponent(
                ProductionSourceReference.feedstockBatch(feedstockBatch.batchId().value()),
                VolumeValue.ofLiters("100")
            ))),
            new ProductionProfileSnapshot(
                SpecificGravityValue.of("1.052"),
                TitratableAcidityValue.of("5.8"),
                TanninityValue.of("1200")
            ),
            null
        ),
        "accepted production input"
    );
  }

  private static FeedstockBatch feedstockBatch(
      CiderMakerId ciderMakerId,
      String name,
      String volumeLiters
  ) {
    return FeedstockBatch.create(
        FeedstockBatchId.newId(),
        ciderMakerId,
        FeedstockBatchName.of(name),
        FeedstockVessel.of("Tank A-01"),
        FeedstockType.JUICE,
        FeedstockSeason.of(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-12-15")),
        VolumeValue.ofLiters(volumeLiters)
    );
  }

  private static ProductionFeedstockTraceSnapshot feedstockSnapshot() {
    return new ProductionFeedstockTraceSnapshot(
        ChemicalProfileAxes.of(
            axis(ChemicalAxis.SUGAR, "1.050"),
            axis(ChemicalAxis.ACIDITY, "6.1"),
            axis(ChemicalAxis.TANNINITY, "1400")
        ),
        ConfidenceLevel.LAB_VERIFIED
    );
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

  private record RecordedTrace(
      ProductionBatch productionBatch,
      List<ProductionBatchTraceSource> sources,
      Instant occurredAt,
      String note
  ) {
  }

  private static final class CapturingTraceRecorder implements ProductionBatchTraceRecorder {

    private RecordedTrace recorded;

    @Override
    public void recordCreated(
        @NonNull ProductionBatch productionBatch,
        @NonNull List<@NonNull ProductionBatchTraceSource> sources,
        @NonNull Instant occurredAt,
        String note
    ) {
      recorded = new RecordedTrace(
          productionBatch,
          List.copyOf(sources),
          occurredAt,
          note
      );
    }
  }

  private static final class InMemoryFeedstockBatchRepository implements FeedstockBatchRepository {

    private final Map<FeedstockBatchId, FeedstockBatch> batches = new HashMap<>();

    private InMemoryFeedstockBatchRepository(FeedstockBatch batch) {
      batches.put(batch.batchId(), batch);
    }

    @Override
    public @NonNull FeedstockBatch save(@NonNull FeedstockBatch feedstockBatch) {
      batches.put(feedstockBatch.batchId(), feedstockBatch);
      return feedstockBatch;
    }

    @Override
    public @NonNull Optional<@NonNull FeedstockBatch> findById(@NonNull FeedstockBatchId batchId) {
      return Optional.ofNullable(batches.get(batchId));
    }

    @Override
    public @NonNull List<@NonNull FeedstockBatch> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId) {
      return batches.values().stream()
          .filter(batch -> batch.ciderMakerId().equals(ciderMakerId))
          .toList();
    }

    @Override
    public void delete(@NonNull FeedstockBatch feedstockBatch) {
      batches.remove(feedstockBatch.batchId());
    }
  }

  private static final class StagingProductionBatchRepository implements ProductionBatchRepository {

    private final StagingTransactionHelper transactionHelper;
    private final List<ProductionBatch> committed = new ArrayList<>();

    private StagingProductionBatchRepository(StagingTransactionHelper transactionHelper) {
      this.transactionHelper = transactionHelper;
    }

    @Override
    public @NonNull ProductionBatch save(@NonNull ProductionBatch productionBatch) {
      transactionHelper.stage(() -> committed.add(productionBatch));
      return productionBatch;
    }

    @Override
    public @NonNull Optional<@NonNull ProductionBatch> findById(@NonNull ProductionBatchId batchId) {
      return committed.stream()
          .filter(batch -> batch.batchId().equals(batchId))
          .findFirst();
    }

    @Override
    public @NonNull List<@NonNull ProductionBatch> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId) {
      return committed.stream()
          .filter(batch -> batch.ciderMakerId().equals(ciderMakerId))
          .toList();
    }
  }

  private static final class StagingTransactionHelper implements TransactionHelper {

    private final List<Runnable> stagedActions = new ArrayList<>();

    @Override
    public @NonNull <T> T callInTransactionNonNull(@NonNull Supplier<T> supplier) {
      stagedActions.clear();
      try {
        final T result = supplier.get();
        stagedActions.forEach(Runnable::run);
        return result;
      } finally {
        stagedActions.clear();
      }
    }

    @Override
    public void executeInTransaction(@NonNull Runnable runnable) {
      callInTransactionNonNull(() -> {
        runnable.run();
        return Boolean.TRUE;
      });
    }

    private void stage(Runnable action) {
      stagedActions.add(action);
    }
  }
}
