package org.craft.mcs.cap.production.candidate.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;
import org.craft.mcs.cap.core.production.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.candidate.application.model.AvailableProductionSource;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateCalculation;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateCalculationRequest;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateSnapshot;
import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateView;
import org.craft.mcs.cap.core.production.candidate.application.port.in.CreateProductionBatchCandidateCommand;
import org.craft.mcs.cap.core.production.candidate.application.port.out.ProductionBatchCandidateCalculator;
import org.craft.mcs.cap.core.production.candidate.application.port.out.ProductionBatchCandidateStore;
import org.craft.mcs.cap.core.production.candidate.application.port.out.ProductionSourceInspector;
import org.craft.mcs.cap.core.production.candidate.application.usecase.CreateProductionBatchCandidateService;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidate;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateStatus;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
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
    final var accessControl = mock(ProductionAccessControl.class);
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
        source.volume(),
        VolumeValue.ofLiters("250"),
        ChemicalProfileAxes.of(
            axis(ChemicalAxis.SUGAR, "1.052"),
            axis(ChemicalAxis.ACIDITY, "6.2"),
            null
        ),
        ConfidenceLevel.LAB_VERIFIED,
        ProductionSourceEvidenceReference.of("measurement-2026-07-16")
    );
  }

  private static @NonNull ProductionBatchCandidateCalculationRequest calculationRequest() {
    return new ProductionBatchCandidateCalculationRequest(
        ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        null,
        ProfileTargetObjectiveWeights.balanced(),
        ProfileTargetScoringPurpose.BLENDING_PLANNING,
        ChemicalProfileCompositionCalculatorCode.MASS_BALANCE,
        ProfileAxisNormalizerCode.SENSITIVITY_UNIT,
        ProfileTargetObjectiveFunctionCode.MANHATTAN_DISTANCE_TO_TARGET_AREA
    );
  }

  private static @NonNull ProductionProfileSnapshot productionProfile() {
    return new ProductionProfileSnapshot(
        SpecificGravityValue.of("1.052"),
        TitratableAcidityValue.of("6.2"),
        null
    );
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
