package org.craft.mcs.cap.production.planning.application.usecase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.craft.mcs.cap.core.production.planning.application.authorization.port.out.ProductionPlanningAccessControl;
import org.craft.mcs.cap.core.production.planning.application.ProductionBatchCandidateStaleException;
import org.craft.mcs.cap.core.production.planning.application.contract.AvailableProductionSource;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculation;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculationRequest;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateView;
import org.craft.mcs.cap.core.production.planning.application.port.in.CreateProductionBatchCandidateCommand;
import org.craft.mcs.cap.core.production.planning.application.port.out.ProductionBatchCandidateCalculator;
import org.craft.mcs.cap.core.production.planning.application.port.out.ProductionBatchCandidateStore;
import org.craft.mcs.cap.core.production.planning.application.port.out.ProductionSourceInspector;
import org.craft.mcs.cap.core.production.planning.application.port.out.ProductionSourceSnapshotValidator;
import org.craft.mcs.cap.core.production.planning.application.usecase.CreateProductionBatchCandidateService;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidateStatus;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.material.value.MaterialAvailability;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.productionCandidatePrediction;
import static org.craft.mcs.cap.production.testing.ProductionSourceCharacteristicsFixtures
    .productionSourceCharacteristics;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class CreateProductionBatchCandidateServiceTest {

  @Test
  void assessesAuthoritativeSourceEvidenceAndSavesAvailableCandidate() {
    final var sourceInspector = mock(ProductionSourceInspector.class);
    final var sourceSnapshotValidator = mock(ProductionSourceSnapshotValidator.class);
    final var calculator = mock(ProductionBatchCandidateCalculator.class);
    final var candidateStore = mock(ProductionBatchCandidateStore.class);
    final var accessControl = mock(ProductionPlanningAccessControl.class);
    final var workspaceId = WorkspaceId.newId();
    final var sourceReference = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
    final var source = new ProductionSourceComponent(sourceReference, VolumeValue.ofLiters("100"));
    final var command = new CreateProductionBatchCandidateCommand(
        workspaceId,
        ProductionSourceComposition.of(List.of(source)),
        calculationRequest()
    );
    final var availableSource = availableSource(source);
    final var profile = productionProfile();
    when(sourceInspector.inspect(workspaceId, source)).thenReturn(availableSource);
    when(calculator.calculate(List.of(availableSource), command.calculationRequest()))
        .thenReturn(new ProductionBatchCandidateCalculation(
            profile,
            productionCandidatePrediction(profile.specificGravity()),
            null,
            null,
            List.of()
        ));
    when(candidateStore.create(any())).thenAnswer(invocation -> invocation.getArgument(0));
    final var service = new CreateProductionBatchCandidateService(
        sourceInspector,
        sourceSnapshotValidator,
        calculator,
        candidateStore,
        accessControl,
        new DirectTransactionHelper()
    );

    final var result = service.create(command);

    final var savedSnapshot = ArgumentCaptor.forClass(ProductionBatchCandidateSnapshot.class);
    verify(accessControl).requireWriteAccess(workspaceId);
    verify(sourceSnapshotValidator).validate(workspaceId, availableSource);
    verify(candidateStore).create(savedSnapshot.capture());
    assertThat(result)
        .returns(workspaceId, ProductionBatchCandidateView::workspaceId)
        .returns(ProductionBatchCandidateStatus.AVAILABLE, ProductionBatchCandidateView::status)
        .returns(profile, ProductionBatchCandidateView::profile)
        .returns(productionCandidatePrediction(profile.specificGravity()), ProductionBatchCandidateView::potentialAbv)
        .returns(null, ProductionBatchCandidateView::normalizerCode);
    assertThat(savedSnapshot.getValue().candidate())
        .returns(command.sourceComposition(), ProductionBatchCandidate::sourceComposition)
        .satisfies(candidate -> assertThat(candidate.evidenceFor(sourceReference))
            .isEqualTo(availableSource.evidenceReference()));
  }

  @Test
  void rejectsChangedSourceAfterCalculationWithoutSavingCandidate() {
    final var sourceInspector = mock(ProductionSourceInspector.class);
    final var sourceSnapshotValidator = mock(ProductionSourceSnapshotValidator.class);
    final var calculator = mock(ProductionBatchCandidateCalculator.class);
    final var candidateStore = mock(ProductionBatchCandidateStore.class);
    final var workspaceId = WorkspaceId.newId();
    final var sourceReference = ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value());
    final var source = new ProductionSourceComponent(sourceReference, VolumeValue.ofLiters("100"));
    final var command = new CreateProductionBatchCandidateCommand(
        workspaceId,
        ProductionSourceComposition.of(List.of(source)),
        calculationRequest()
    );
    final var availableSource = availableSource(source);
    final var profile = productionProfile();
    when(sourceInspector.inspect(workspaceId, source)).thenReturn(availableSource);
    when(calculator.calculate(List.of(availableSource), command.calculationRequest()))
        .thenReturn(new ProductionBatchCandidateCalculation(
            profile,
            productionCandidatePrediction(profile.specificGravity()),
            null,
            null,
            List.of()
        ));
    doThrow(new ProductionBatchCandidateStaleException(sourceReference))
        .when(sourceSnapshotValidator)
        .validate(workspaceId, availableSource);
    final var service = new CreateProductionBatchCandidateService(
        sourceInspector,
        sourceSnapshotValidator,
        calculator,
        candidateStore,
        mock(ProductionPlanningAccessControl.class),
        new DirectTransactionHelper()
    );

    assertThatThrownBy(() -> service.create(command))
        .isInstanceOf(ProductionBatchCandidateStaleException.class);
    verify(calculator).calculate(List.of(availableSource), command.calculationRequest());
    verify(candidateStore, never()).create(any());
  }

  @Test
  void calculatesOutsideTransactionsAndValidatesSourcesInStableOrder() {
    final var sourceInspector = mock(ProductionSourceInspector.class);
    final var sourceSnapshotValidator = mock(ProductionSourceSnapshotValidator.class);
    final var calculator = mock(ProductionBatchCandidateCalculator.class);
    final var candidateStore = mock(ProductionBatchCandidateStore.class);
    final var workspaceId = WorkspaceId.newId();
    final var first = sourceComponent("40");
    final var second = sourceComponent("60");
    final var command = new CreateProductionBatchCandidateCommand(
        workspaceId,
        ProductionSourceComposition.of(List.of(second, first)),
        calculationRequest()
    );
    final var firstSnapshot = availableSource(first);
    final var secondSnapshot = availableSource(second);
    final var transactionHelper = new TrackingTransactionHelper();
    final var validationOrder = new ArrayList<ProductionSourceReference>();
    when(sourceInspector.inspect(workspaceId, first)).thenAnswer(invocation -> {
      assertThat(transactionHelper.active()).isTrue();
      return firstSnapshot;
    });
    when(sourceInspector.inspect(workspaceId, second)).thenAnswer(invocation -> {
      assertThat(transactionHelper.active()).isTrue();
      return secondSnapshot;
    });
    when(calculator.calculate(List.of(secondSnapshot, firstSnapshot), command.calculationRequest()))
        .thenAnswer(invocation -> {
          assertThat(transactionHelper.active()).isFalse();
          final var profile = productionProfile();
          return new ProductionBatchCandidateCalculation(
              profile,
              productionCandidatePrediction(profile.specificGravity()),
              null,
              null,
              List.of()
          );
        });
    doAnswer(invocation -> {
      assertThat(transactionHelper.active()).isTrue();
      validationOrder.add(invocation.<AvailableProductionSource>getArgument(1).reference());
      return null;
    }).when(sourceSnapshotValidator).validate(eq(workspaceId), any());
    when(candidateStore.create(any())).thenAnswer(invocation -> {
      assertThat(transactionHelper.active()).isTrue();
      return invocation.getArgument(0);
    });
    final var service = new CreateProductionBatchCandidateService(
        sourceInspector,
        sourceSnapshotValidator,
        calculator,
        candidateStore,
        mock(ProductionPlanningAccessControl.class),
        transactionHelper
    );

    service.create(command);

    assertThat(transactionHelper.transactionCount()).isEqualTo(2);
    assertThat(validationOrder).containsExactlyElementsOf(
        Stream.of(first.reference(), second.reference())
            .sorted(Comparator.comparing(ProductionSourceReference::type)
                .thenComparing(ProductionSourceReference::id))
            .toList()
    );
  }

  private static @NonNull ProductionSourceComponent sourceComponent(@NonNull String volume) {
    return new ProductionSourceComponent(
        ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value()),
        VolumeValue.ofLiters(volume)
    );
  }

  private static @NonNull AvailableProductionSource availableSource(
      @NonNull ProductionSourceComponent source
  ) {
    return new AvailableProductionSource(
        source.reference(),
        null,
        AggregateRevision.of(1),
        new MaterialAvailability(
            source.volume(),
            VolumeValue.ofLiters("250"),
            MaterialStateId.newId()
        ),
        productionSourceCharacteristics("1.052", "6.2", null, null),
        ConfidenceLevel.LAB_VERIFIED,
        ProductionSourceEvidenceReference.of("measurement-2026-07-16")
    );
  }

  private static @NonNull ProductionBatchCandidateCalculationRequest calculationRequest() {
    return new ProductionBatchCandidateCalculationRequest(
        ProductionBatchCandidateCalculationRequest.TanninPolicy.CALCULATE_WHEN_COMPLETE,
        null,
        ProductionBatchCandidateCalculationRequest.Calculator.MASS_BALANCE
    );
  }

  private static @NonNull ProductionProfileSnapshot productionProfile() {
    return new ProductionProfileSnapshot(
        SpecificGravityValue.of("1.052"),
        TitratableAcidityValue.of("6.2"),
        null
    );
  }

  private static final class DirectTransactionHelper implements TransactionHelper {

    @Override
    public @NonNull <T> T callInTransactionNonNull(@NonNull Supplier<T> supplier) {
      return supplier.get();
    }

    @Override
    public void executeInTransaction(@NonNull Runnable runnable) {
      runnable.run();
    }
  }

  private static final class TrackingTransactionHelper implements TransactionHelper {

    private boolean active;
    private int transactionCount;

    @Override
    public @NonNull <T> T callInTransactionNonNull(@NonNull Supplier<T> supplier) {
      active = true;
      transactionCount++;
      try {
        return supplier.get();
      } finally {
        active = false;
      }
    }

    @Override
    public void executeInTransaction(@NonNull Runnable runnable) {
      callInTransactionNonNull(() -> {
        runnable.run();
        return Boolean.TRUE;
      });
    }

    boolean active() {
      return active;
    }

    int transactionCount() {
      return transactionCount;
    }
  }
}
