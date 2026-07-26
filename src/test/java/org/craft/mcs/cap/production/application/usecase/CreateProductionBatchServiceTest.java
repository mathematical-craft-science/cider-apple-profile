package org.craft.mcs.cap.production.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.production.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.application.model.ProductionBatchView;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.application.port.out.ConsumedProductionSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchFeedstockTraceSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceRecorder;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchTraceSource;
import org.craft.mcs.cap.core.production.application.port.out.ProductionSourceDisplayName;
import org.craft.mcs.cap.core.production.application.port.out.ProductionSourceInventory;
import org.craft.mcs.cap.core.production.application.usecase.CreateProductionBatchService;
import org.craft.mcs.cap.core.production.candidate.application.ProductionBatchCandidateStaleException;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateView;
import org.craft.mcs.cap.core.production.candidate.application.port.out.ProductionBatchCandidateStore;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateStatus;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.domain.model.value.AlcoholicFermentationState;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.prediction;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;


class CreateProductionBatchServiceTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-05-18T10:15:30Z"), ZoneOffset.UTC);
  private static final ProductionSourceEvidenceReference EVIDENCE =
      ProductionSourceEvidenceReference.of("measurement-2026-05-18");
  private static final MaterialStateId MATERIAL_STATE = MaterialStateId.newId();

  @Test
  void confirmsCandidateCreatesProductionBatchAndRecordsTraceInSameTransaction() {
    final var transactionHelper = new StagingTransactionHelper();
    final var workspaceId = WorkspaceId.newId();
    final var sourceReference = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
    final var candidate = candidate(workspaceId, sourceReference);
    final var candidateStore = new StagingCandidateStore(transactionHelper, candidate);
    final var productionRepository = new StagingProductionBatchRepository(transactionHelper);
    final var traceability = new CapturingTraceRecorder();
    final var service = service(
        sourceInventory(EVIDENCE),
        candidateStore,
        productionRepository,
        traceability,
        transactionHelper
    );

    final var created = service.create(command(workspaceId, candidate.candidate().candidateId()));

    assertThat(productionRepository.committed)
        .hasSize(1);
    assertThat(created.productionBatch())
        .returns(Vessel.named("Tank F-03"), ProductionBatchView::vessel)
        .returns(BatchLifecycle.ACTIVE, ProductionBatchView::lifecycle)
        .returns(
            AlcoholicFermentationState.NOT_STARTED,
            ProductionBatchView::alcoholicFermentationState
        );
    assertThat(created.productionBatch().initialProfileSnapshot())
        .returns(SpecificGravityValue.of("1.050"), ProductionProfileSnapshot::specificGravity)
        .returns(TitratableAcidityValue.of("6.1"), ProductionProfileSnapshot::titratableAcidity)
        .returns(TanninValue.of("1400"), ProductionProfileSnapshot::tannin);
    assertThat(created.candidate())
        .returns(ProductionBatchCandidateStatus.CONFIRMED, ProductionBatchCandidateView::status)
        .returns(
            created.productionBatch().batchId(),
            ProductionBatchCandidateView::confirmedProductionBatchId
        );
    assertThat(candidateStore.committed.candidate())
        .returns(ProductionBatchCandidateStatus.CONFIRMED, ProductionBatchCandidate::status)
        .returns(
            created.productionBatch().batchId(),
            ProductionBatchCandidate::confirmedProductionBatchId
        );
    assertThat(traceability.recorded)
        .isNotNull()
        .satisfies(recorded -> assertThat(recorded.productionBatch().batchId())
            .isEqualTo(created.productionBatch().batchId()))
        .satisfies(recorded -> assertThat(recorded.sources()).singleElement()
            .satisfies(source -> {
              assertThat(source)
                  .isInstanceOf(ProductionBatchFeedstockTraceSource.class)
                  .returns(sourceReference.id(), ProductionBatchTraceSource::id)
                  .returns(ProductionSourceDisplayName.of("Late juice"), ProductionBatchTraceSource::displayName)
                  .returns(VolumeValue.ofLiters("250"), ProductionBatchTraceSource::availableBefore)
                  .returns(VolumeValue.ofLiters("100"), ProductionBatchTraceSource::consumed)
                  .returns(VolumeValue.ofLiters("150"), ProductionBatchTraceSource::remaining)
                  .returns(ConfidenceLevel.LAB_VERIFIED, ProductionBatchTraceSource::confidence);
              assertThat(source.profile().valueOf(ChemicalAxis.SUGAR))
                  .get()
                  .returns(new BigDecimal("1.050"), AxisValue::value);
            }))
        .returns(Instant.now(CLOCK), RecordedTrace::occurredAt)
        .returns("accepted production input", RecordedTrace::note);
  }

  @Test
  void rollsBackProductionAndCandidateConfirmationWhenTraceAppendFails() {
    final var transactionHelper = new StagingTransactionHelper();
    final var workspaceId = WorkspaceId.newId();
    final var sourceReference = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
    final var candidate = candidate(workspaceId, sourceReference);
    final var candidateStore = new StagingCandidateStore(transactionHelper, candidate);
    final var productionRepository = new StagingProductionBatchRepository(transactionHelper);
    final var service = service(
        sourceInventory(EVIDENCE),
        candidateStore,
        productionRepository,
        (productionBatch, sources, occurredAt, note) -> {
          throw new IllegalStateException("trace persistence failed");
        },
        transactionHelper
    );

    assertThatThrownBy(() -> service.create(command(workspaceId, candidate.candidate().candidateId())))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("trace persistence failed");
    assertThat(productionRepository.committed)
        .isEmpty();
    assertThat(candidateStore.committed.candidate().status())
        .isEqualTo(ProductionBatchCandidateStatus.AVAILABLE);
  }

  @Test
  void rejectsCandidateWhenCurrentSourceEvidenceChanged() {
    final var transactionHelper = new StagingTransactionHelper();
    final var workspaceId = WorkspaceId.newId();
    final var sourceReference = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
    final var candidate = candidate(workspaceId, sourceReference);
    final var candidateStore = new StagingCandidateStore(transactionHelper, candidate);
    final var productionRepository = new StagingProductionBatchRepository(transactionHelper);
    final var service = service(
        sourceInventory(ProductionSourceEvidenceReference.of("new-measurement")),
        candidateStore,
        productionRepository,
        new CapturingTraceRecorder(),
        transactionHelper
    );

    assertThatThrownBy(() -> service.create(command(workspaceId, candidate.candidate().candidateId())))
        .isInstanceOf(ProductionBatchCandidateStaleException.class);
    assertThat(productionRepository.committed)
        .isEmpty();
    assertThat(candidateStore.committed.candidate().status())
        .isEqualTo(ProductionBatchCandidateStatus.AVAILABLE);
  }

  @Test
  void rejectsCandidateWhenSourceMaterialStateChanged() {
    final var transactionHelper = new StagingTransactionHelper();
    final var workspaceId = WorkspaceId.newId();
    final var sourceReference = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
    final var candidate = candidate(workspaceId, sourceReference);
    final var candidateStore = new StagingCandidateStore(transactionHelper, candidate);
    final var productionRepository = new StagingProductionBatchRepository(transactionHelper);
    final var service = service(
        sourceInventory(EVIDENCE, MaterialStateId.newId()),
        candidateStore,
        productionRepository,
        new CapturingTraceRecorder(),
        transactionHelper
    );

    assertThatThrownBy(() -> service.create(command(workspaceId, candidate.candidate().candidateId())))
        .isInstanceOf(ProductionBatchCandidateStaleException.class);
    assertThat(productionRepository.committed)
        .isEmpty();
    assertThat(candidateStore.committed.candidate().status())
        .isEqualTo(ProductionBatchCandidateStatus.AVAILABLE);
  }

  @Test
  void repeatedCandidateConfirmationDoesNotConsumeSourceAgain() {
    final var transactionHelper = new StagingTransactionHelper();
    final var workspaceId = WorkspaceId.newId();
    final var sourceReference = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
    final var candidate = candidate(workspaceId, sourceReference);
    final var candidateStore = new StagingCandidateStore(transactionHelper, candidate);
    final var productionRepository = new StagingProductionBatchRepository(transactionHelper);
    final var sourceConsumptionCount = new AtomicInteger();
    final var sourceInventory = sourceInventory(EVIDENCE);
    final ProductionSourceInventory countingInventory = (requestedWorkspaceId, source) -> {
      sourceConsumptionCount.incrementAndGet();
      return sourceInventory.consume(requestedWorkspaceId, source);
    };
    final var service = service(
        countingInventory,
        candidateStore,
        productionRepository,
        new CapturingTraceRecorder(),
        transactionHelper
    );
    final var command = command(workspaceId, candidate.candidate().candidateId());

    service.create(command);

    assertThatThrownBy(() -> service.create(command))
        .isInstanceOf(DomainConflictException.class)
        .hasMessageContaining("already been confirmed");
    assertThat(sourceConsumptionCount)
        .hasValue(1);
    assertThat(productionRepository.committed)
        .hasSize(1);
  }

  private static @NonNull CreateProductionBatchService service(
      @NonNull ProductionSourceInventory sourceInventory,
      @NonNull ProductionBatchCandidateStore candidateStore,
      @NonNull ProductionBatchRepository productionRepository,
      @NonNull ProductionBatchTraceRecorder traceRecorder,
      @NonNull TransactionHelper transactionHelper
  ) {
    return new CreateProductionBatchService(
        sourceInventory,
        candidateStore,
        productionRepository,
        traceRecorder,
        mock(ProductionAccessControl.class),
        transactionHelper,
        CLOCK
    );
  }

  private static @NonNull CreateProductionBatchCommand command(
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionBatchCandidateId candidateId
  ) {
    return new CreateProductionBatchCommand(
        workspaceId,
        BatchName.of("Classic cider path"),
        Vessel.named("Tank F-03"),
        candidateId,
        "accepted production input"
    );
  }

  private static @NonNull ProductionBatchCandidateSnapshot candidate(
      @NonNull WorkspaceId workspaceId,
      @NonNull ProductionSourceReference sourceReference
  ) {
    final var composition = ProductionSourceComposition.of(List.of(new ProductionSourceComponent(
        sourceReference,
        VolumeValue.ofLiters("100")
    )));
    return new ProductionBatchCandidateSnapshot(
        ProductionBatchCandidate.create(
            ProductionBatchCandidateId.newId(),
            workspaceId,
            composition,
            Map.of(sourceReference, EVIDENCE),
            Map.of(sourceReference, MATERIAL_STATE),
            productionProfile()
        ),
        prediction(productionProfile().specificGravity()),
        null,
        null,
        List.of()
    );
  }

  private static @NonNull ProductionProfileSnapshot productionProfile() {
    return new ProductionProfileSnapshot(
        SpecificGravityValue.of("1.050"),
        TitratableAcidityValue.of("6.1"),
        TanninValue.of("1400")
    );
  }

  private static @NonNull ProductionSourceInventory sourceInventory(
      @NonNull ProductionSourceEvidenceReference evidenceReference
  ) {
    return sourceInventory(evidenceReference, MATERIAL_STATE);
  }

  private static @NonNull ProductionSourceInventory sourceInventory(
      @NonNull ProductionSourceEvidenceReference evidenceReference,
      @NonNull MaterialStateId materialStateId
  ) {
    return (workspaceId, source) -> new ConsumedProductionSource(
        source.reference(),
        ProductionSourceDisplayName.of("Late juice"),
        VolumeValue.ofLiters("250"),
        source.volume(),
        VolumeValue.ofLiters("150"),
        ChemicalProfileAxes.of(
            axis(ChemicalAxis.SUGAR, "1.050"),
            axis(ChemicalAxis.ACIDITY, "6.1"),
            axis(ChemicalAxis.TANNIN, "1400")
        ),
        ConfidenceLevel.LAB_VERIFIED,
        materialStateId,
        MaterialStateId.newId(),
        evidenceReference
    );
  }

  private static @NonNull AxisValue axis(
      @NonNull ChemicalAxis axis,
      @NonNull String value
  ) {
    return new AxisValue(
        axis,
        new BigDecimal(value),
        AxisValueKind.POINT,
        null,
        null
    );
  }

  private record RecordedTrace(
      @NonNull ProductionBatch productionBatch,
      @NonNull List<@NonNull ProductionBatchTraceSource> sources,
      @NonNull Instant occurredAt,
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
      recorded = new RecordedTrace(productionBatch, List.copyOf(sources), occurredAt, note);
    }
  }

  private static final class StagingProductionBatchRepository implements ProductionBatchRepository {

    private final StagingTransactionHelper transactionHelper;
    private final List<ProductionBatch> committed = new ArrayList<>();

    private StagingProductionBatchRepository(@NonNull StagingTransactionHelper transactionHelper) {
      this.transactionHelper = transactionHelper;
    }

    @Override
    public @NonNull ProductionBatch create(@NonNull ProductionBatch productionBatch) {
      transactionHelper.stage(() -> committed.add(productionBatch));
      return productionBatch;
    }

    @Override
    public @NonNull VersionedAggregate<ProductionBatch> update(
        @NonNull VersionedAggregate<ProductionBatch> productionBatch
    ) {
      transactionHelper.stage(() -> {
        committed.removeIf(batch -> batch.batchId().equals(productionBatch.aggregate().batchId()));
        committed.add(productionBatch.aggregate());
      });
      return new VersionedAggregate<>(
          productionBatch.aggregate(),
          AggregateRevision.of(productionBatch.revision().value() + 1)
      );
    }

    @Override
    public @NonNull Optional<@NonNull VersionedAggregate<ProductionBatch>> findVersionedById(
        @NonNull ProductionBatchId batchId
    ) {
      return committed.stream()
          .filter(batch -> batch.batchId().equals(batchId))
          .findFirst()
          .map(batch -> new VersionedAggregate<>(batch, AggregateRevision.of(1)));
    }
  }

  private static final class StagingCandidateStore implements ProductionBatchCandidateStore {

    private final StagingTransactionHelper transactionHelper;
    private ProductionBatchCandidateSnapshot committed;

    private StagingCandidateStore(
        @NonNull StagingTransactionHelper transactionHelper,
        @NonNull ProductionBatchCandidateSnapshot initial
    ) {
      this.transactionHelper = transactionHelper;
      committed = copy(initial);
    }

    @Override
    public @NonNull ProductionBatchCandidateSnapshot create(
        @NonNull ProductionBatchCandidateSnapshot snapshot
    ) {
      final var saved = copy(snapshot);
      transactionHelper.stage(() -> committed = saved);
      return saved;
    }

    @Override
    public @NonNull ProductionBatchCandidateSnapshot update(
        @NonNull ProductionBatchCandidateSnapshot snapshot
    ) {
      final var saved = copy(snapshot);
      transactionHelper.stage(() -> committed = saved);
      return saved;
    }

    @Override
    public @NonNull Optional<@NonNull ProductionBatchCandidateSnapshot> findById(
        @NonNull ProductionBatchCandidateId candidateId
    ) {
      return committed.candidate().candidateId().equals(candidateId)
          ? Optional.of(copy(committed))
          : Optional.empty();
    }

    private static @NonNull ProductionBatchCandidateSnapshot copy(
        @NonNull ProductionBatchCandidateSnapshot snapshot
    ) {
      final var candidate = snapshot.candidate();
      return new ProductionBatchCandidateSnapshot(
          ProductionBatchCandidate.restore(
              candidate.candidateId(),
              candidate.workspaceId(),
              candidate.sourceComposition(),
              candidate.sourceEvidence(),
              candidate.sourceMaterialStates(),
              candidate.profile(),
              candidate.status(),
              candidate.confirmedProductionBatchId()
          ),
          snapshot.potentialAbv(),
          snapshot.normalizerCode(),
          snapshot.targetDistance(),
          snapshot.warnings(),
          snapshot.revision()
      );
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

    private void stage(@NonNull Runnable action) {
      stagedActions.add(action);
    }
  }
}
