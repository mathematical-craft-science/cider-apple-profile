package org.craft.mcs.cap.infrastructure.adapter.out.production.planning.composition;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.formulation.calculation.application.FormulationTargetFitUnavailableException;
import org.craft.mcs.cap.core.formulation.calculation.application.InvalidCompositionRequestException;
import org.craft.mcs.cap.core.formulation.calculation.application.UnsupportedCompositionCalculatorRequestException;
import org.craft.mcs.cap.core.formulation.calculation.application.port.in.CalculateCompositionCommand;
import org.craft.mcs.cap.core.formulation.calculation.application.port.in.CalculateCompositionUseCase;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceEvidenceReference;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.core.production.planning.application.InvalidProductionCandidateCalculationException;
import org.craft.mcs.cap.core.production.planning.application.ProductionCandidateTargetFitUnavailableException;
import org.craft.mcs.cap.core.production.planning.application.UnsupportedProductionCandidateCalculationException;
import org.craft.mcs.cap.core.production.planning.application.contract.AvailableProductionSource;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculationRequest;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.material.value.MaterialAvailability;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.production.testing.ProductionSourceCharacteristicsFixtures
    .productionSourceCharacteristics;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class FormulationProductionBatchCandidateCalculatorTest {

  @Test
  void mapsProductionSourceAndTranslatesInvalidFormulationRequest() {
    final var useCase = mock(CalculateCompositionUseCase.class);
    final var cause = new InvalidCompositionRequestException(
        "invalid formulation request",
        new RuntimeException("domain cause")
    );
    when(useCase.calculate(any())).thenThrow(cause);
    final var calculator = new FormulationProductionBatchCandidateCalculator(useCase);
    final var source = source();

    assertThatThrownBy(() -> calculator.calculate(List.of(source), request()))
        .isInstanceOf(InvalidProductionCandidateCalculationException.class)
        .hasMessage("invalid formulation request")
        .hasCause(cause);

    final var command = ArgumentCaptor.forClass(CalculateCompositionCommand.class);
    verify(useCase).calculate(command.capture());
    assertThat(command.getValue().sources().getFirst().reference())
        .satisfies(reference -> {
          assertThat(reference.type()).isEqualTo(source.reference().type().name());
          assertThat(reference.id()).isEqualTo(source.reference().id().toString());
        });
  }

  @Test
  void translatesUnsupportedFormulationCalculator() {
    final var useCase = mock(CalculateCompositionUseCase.class);
    final var cause = new UnsupportedCompositionCalculatorRequestException(
        "unsupported formulation calculator",
        new RuntimeException("domain cause")
    );
    when(useCase.calculate(any())).thenThrow(cause);

    assertThatThrownBy(() -> calculator(useCase).calculate(List.of(source()), request()))
        .isInstanceOf(UnsupportedProductionCandidateCalculationException.class)
        .hasMessage("unsupported formulation calculator")
        .hasCause(cause);
  }

  @Test
  void translatesUnavailableTargetToProductionPlanningLanguage() {
    final var useCase = mock(CalculateCompositionUseCase.class);
    final var cause = new FormulationTargetFitUnavailableException(
        "target unavailable",
        new RuntimeException("analysis cause")
    );
    when(useCase.calculate(any())).thenThrow(cause);

    assertThatThrownBy(() -> calculator(useCase).calculate(List.of(source()), request()))
        .isInstanceOf(ProductionCandidateTargetFitUnavailableException.class)
        .hasMessage("target unavailable")
        .hasCause(cause);
  }

  private static @NonNull FormulationProductionBatchCandidateCalculator calculator(
      @NonNull CalculateCompositionUseCase useCase
  ) {
    return new FormulationProductionBatchCandidateCalculator(useCase);
  }

  private static @NonNull AvailableProductionSource source() {
    final var selectedVolume = VolumeValue.ofLiters("60");
    return new AvailableProductionSource(
        ProductionSourceReference.feedstockBatch(FeedstockBatchId.newId().value()),
        null,
        AggregateRevision.of(0),
        new MaterialAvailability(
            selectedVolume,
            VolumeValue.ofLiters("100"),
            MaterialStateId.newId()
        ),
        productionSourceCharacteristics("1.060", "6.0", null, null),
        null,
        ProductionSourceEvidenceReference.of("measurement-evidence")
    );
  }

  private static @NonNull ProductionBatchCandidateCalculationRequest request() {
    return new ProductionBatchCandidateCalculationRequest(
        ProductionBatchCandidateCalculationRequest.TanninPolicy.CALCULATE_WHEN_COMPLETE,
        null,
        ProductionBatchCandidateCalculationRequest.Calculator.MASS_BALANCE
    );
  }
}
