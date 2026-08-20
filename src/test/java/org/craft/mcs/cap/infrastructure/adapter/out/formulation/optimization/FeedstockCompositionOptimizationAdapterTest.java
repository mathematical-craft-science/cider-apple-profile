package org.craft.mcs.cap.infrastructure.adapter.out.formulation.optimization;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.InvalidFeedstockCompositionOptimizationException;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.UnsupportedFeedstockCompositionOptimizationException;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.contract.FeedstockPlanningGraphView.Zone;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.FeedstockTargetFitUnavailableException;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.formulation.calculation.application.FormulationTargetFitUnavailableException;
import org.craft.mcs.cap.core.formulation.optimization.application.InvalidCompositionOptimizationRequestException;
import org.craft.mcs.cap.core.formulation.optimization.application.UnsupportedCompositionOptimizationRequestException;
import org.craft.mcs.cap.core.formulation.optimization.application.port.in.OptimizeCompositionCommand;
import org.craft.mcs.cap.core.formulation.optimization.application.port.in.OptimizeCompositionUseCase;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class FeedstockCompositionOptimizationAdapterTest {

  @Test
  void mapsFeedstockIdentityAndTranslatesInvalidFormulationRequest() {
    final var useCase = mock(OptimizeCompositionUseCase.class);
    final var cause = new InvalidCompositionOptimizationRequestException(
        "invalid formulation request",
        new RuntimeException("domain cause")
    );
    when(useCase.optimize(any())).thenThrow(cause);
    final var adapter = new FeedstockCompositionOptimizationAdapter(useCase);
    final var request = request();

    assertThatThrownBy(() -> adapter.optimize(request))
        .isInstanceOf(InvalidFeedstockCompositionOptimizationException.class)
        .hasMessage("invalid formulation request")
        .hasCause(cause);

    final var command = ArgumentCaptor.forClass(OptimizeCompositionCommand.class);
    verify(useCase).optimize(command.capture());
    assertThat(command.getValue().sources().getFirst().reference())
        .satisfies(reference -> {
          assertThat(reference.type()).isEqualTo("FEEDSTOCK_BATCH");
          assertThat(reference.id()).isEqualTo(request.sources().getFirst().batchId().value().toString());
        });
  }

  @Test
  void translatesUnsupportedFormulationStrategy() {
    final var useCase = mock(OptimizeCompositionUseCase.class);
    final var cause = new UnsupportedCompositionOptimizationRequestException(
        "unsupported formulation strategy",
        new RuntimeException("domain cause")
    );
    when(useCase.optimize(any())).thenThrow(cause);

    assertThatThrownBy(() -> new FeedstockCompositionOptimizationAdapter(useCase).optimize(request()))
        .isInstanceOf(UnsupportedFeedstockCompositionOptimizationException.class)
        .hasMessage("unsupported formulation strategy")
        .hasCause(cause);
  }

  @Test
  void translatesUnavailableTargetToFeedstockLanguage() {
    final var useCase = mock(OptimizeCompositionUseCase.class);
    final var cause = new FormulationTargetFitUnavailableException(
        "target unavailable",
        new RuntimeException("analysis cause")
    );
    when(useCase.optimize(any())).thenThrow(cause);

    assertThatThrownBy(() -> new FeedstockCompositionOptimizationAdapter(useCase).optimize(request()))
        .isInstanceOf(FeedstockTargetFitUnavailableException.class)
        .hasMessage("target unavailable")
        .hasCause(cause);
  }

  private static @NonNull FeedstockCompositionOptimizationRequest request() {
    return new FeedstockCompositionOptimizationRequest(
        List.of(new FeedstockCompositionOptimizationRequest.Source(
            FeedstockBatchId.newId(),
            new FeedstockCompositionOptimizationRequest.Characteristics(
                PointCharacteristicValue.of(SpecificGravityValue.of("1.060")),
                PointCharacteristicValue.of(TitratableAcidityValue.of("6.0")),
                null,
                null
            ),
            VolumeValue.ofLiters("100")
        )),
        FeedstockCompositionOptimizationRequest.TanninPolicy.CALCULATE_WHEN_COMPLETE,
        new PredefinedFeedstockProfileTargetSelection(
            Zone.PERFECT_APPLES
        ),
        null,
        new FeedstockCompositionOptimizationRequest.Scoring(
            FeedstockTargetFitConfiguration.Normalizer.EFFECTIVE_RANGE,
            FeedstockTargetFitConfiguration.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
            new FeedstockTargetFitConfiguration.ObjectiveWeights(
                BigDecimal.ONE,
                BigDecimal.ONE,
                null
            )
        ),
        List.of(),
        FeedstockCompositionOptimizationRequest.Settings.defaults(),
        FeedstockCompositionOptimizationRequest.Mode.PROPORTION,
        FeedstockCompositionOptimizationRequest.Algorithm.DISCRETIZED_BOUNDED_SEARCH,
        FeedstockCompositionOptimizationRequest.Calculator.MASS_BALANCE,
        null
    );
  }
}
