package org.craft.mcs.cap.production.planning.application.usecase;

import java.util.List;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.production.planning.application.authorization.port.out.ProductionPlanningAccessControl;
import org.craft.mcs.cap.core.production.planning.application.model.AvailableProductionSource;
import org.craft.mcs.cap.core.production.planning.application.model.ProductionBatchCandidateCalculation;
import org.craft.mcs.cap.core.production.planning.application.model.ProductionBatchCandidateCalculationRequest;
import org.craft.mcs.cap.core.production.planning.application.model.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.planning.application.model.ProductionBatchCandidateView;
import org.craft.mcs.cap.core.production.planning.application.port.in.CreateProductionBatchCandidateCommand;
import org.craft.mcs.cap.core.production.planning.application.port.out.ProductionBatchCandidateCalculator;
import org.craft.mcs.cap.core.production.planning.application.port.out.ProductionBatchCandidateStore;
import org.craft.mcs.cap.core.production.planning.application.port.out.ProductionSourceInspector;
import org.craft.mcs.cap.core.production.planning.application.usecase.CreateProductionBatchCandidateService;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.production.planning.domain.model.ProductionBatchCandidateStatus;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.material.value.MaterialAvailability;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.prediction;
import static org.craft.mcs.cap.production.testing.ProductionSourceCharacteristicsFixtures
    .productionSourceCharacteristics;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class CreateProductionBatchCandidateServiceTest {

  @Test
  void assessesAuthoritativeSourceEvidenceAndSavesAvailableCandidate() {
    final var sourceInspector = mock(ProductionSourceInspector.class);
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
            prediction(profile.specificGravity()),
            ProfileAxisNormalizerCode.SENSITIVITY_UNIT,
            null,
            List.of()
        ));
    when(candidateStore.create(any())).thenAnswer(invocation -> invocation.getArgument(0));
    final var service = new CreateProductionBatchCandidateService(
        sourceInspector,
        calculator,
        candidateStore,
        accessControl,
        new DirectTransactionHelper()
    );

    final var result = service.create(command);

    final var savedSnapshot = ArgumentCaptor.forClass(ProductionBatchCandidateSnapshot.class);
    verify(accessControl).requireWriteAccess(workspaceId);
    verify(candidateStore).create(savedSnapshot.capture());
    assertThat(result)
        .returns(workspaceId, ProductionBatchCandidateView::workspaceId)
        .returns(ProductionBatchCandidateStatus.AVAILABLE, ProductionBatchCandidateView::status)
        .returns(profile, ProductionBatchCandidateView::profile)
        .returns(prediction(profile.specificGravity()), ProductionBatchCandidateView::potentialAbv)
        .returns(ProfileAxisNormalizerCode.SENSITIVITY_UNIT, ProductionBatchCandidateView::normalizerCode);
    assertThat(savedSnapshot.getValue().candidate())
        .returns(command.sourceComposition(), ProductionBatchCandidate::sourceComposition)
        .satisfies(candidate -> assertThat(candidate.evidenceFor(sourceReference))
            .isEqualTo(availableSource.evidenceReference()));
  }

  private static @NonNull AvailableProductionSource availableSource(
      @NonNull ProductionSourceComponent source
  ) {
    return new AvailableProductionSource(
        source.reference(),
        null,
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
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        null,
        new ProfileTargetScoringConfiguration(
            ProfileAxisNormalizerCode.SENSITIVITY_UNIT,
            ProfileTargetObjectiveFunctionCode.MANHATTAN_DISTANCE_TO_TARGET_AREA,
            ProfileTargetObjectiveWeights.balanced()
        ),
        CompositionCalculatorCode.MASS_BALANCE
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
}
