package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CompositionSourceInput;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationCalculator;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTanninPolicy;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition;
import org.craft.mcs.cap.core.formulation.calculation.domain.BuiltInCompositionCalculatorSelector;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationSourceInput;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationMode;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationSettings;
import org.craft.mcs.cap.core.formulation.optimization.application.port.in.OptimizeCompositionCommand;
import org.craft.mcs.cap.core.formulation.optimization.application.usecase.OptimizeCompositionService;
import org.craft.mcs.cap.core.formulation.optimization.domain.strategy.BuiltInCompositionOptimizationStrategySelector;
import org.craft.mcs.cap.core.formulation.interpretation.application.usecase.CompositionGraphInterpretationService;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.CompositionSourceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.CompositionOptimizationCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.classification.NorthAmericanTanninLevel;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange.closed;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures.formulationAbvAnalyzer;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures.formulationTanninLevelAnalyzer;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures.formulationTargetFitAnalyzer;


class CompositionOptimizationRestMapperTest {

  @Test
  void mapsPotentialAbvForEveryOptimizationCandidate() {
    final var result = service().optimize(command(null));

    final var response = CompositionOptimizationRestMapper.toResponse(
        graphInterpreter().interpret(result)
    );

    assertThat(response.candidates())
        .isNotEmpty()
        .allSatisfy(candidate -> assertThat(candidate)
            .satisfies(value -> assertThat(value.northAmericanTanninLevel()).isNull())
            .satisfies(value -> assertThat(value.potentialAbv())
                .isNotNull()
                .satisfies(prediction -> assertThat(prediction.originalGravity().value())
                    .isEqualByComparingTo(value.characteristics().specificGravity().value()))
                .satisfies(prediction -> assertThat(prediction.potentialAbv().unit()).isEqualTo("% ABV")))
            .satisfies(value -> assertThat(value.sources())
                .allSatisfy(source -> assertThat(source.proportion())
                    .isNotNull()
                    .isGreaterThan(BigDecimal.ZERO)
                    .isLessThanOrEqualTo(BigDecimal.ONE))
                .satisfies(sources -> assertThat(sources.stream()
                    .map(CompositionOptimizationRestMapperTest::requiredProportion)
                    .reduce(BigDecimal.ZERO, BigDecimal::add))
                    .isEqualByComparingTo(BigDecimal.ONE))));
  }

  @Test
  void mapsNorthAmericanTanninLevelForEveryOptimizationCandidate() {
    final var response = CompositionOptimizationRestMapper.toResponse(
        graphInterpreter().interpret(service().optimize(command("2000")))
    );

    assertThat(response.candidates())
        .isNotEmpty()
        .extracting(CompositionOptimizationCandidateResponse::northAmericanTanninLevel)
        .containsOnly(NorthAmericanTanninLevel.MEDIUM);
  }

  private static @NonNull BigDecimal requiredProportion(
      @NonNull CompositionSourceResponse source
  ) {
    return requireNonNull(source.proportion(), "candidate source proportion cannot be null");
  }

  private static @NonNull OptimizeCompositionService service() {
    return new OptimizeCompositionService(
        new BuiltInCompositionOptimizationStrategySelector(),
        new BuiltInCompositionCalculatorSelector(),
        formulationTargetFitAnalyzer(),
        formulationAbvAnalyzer()
    );
  }

  private static @NonNull CompositionGraphInterpretationService graphInterpreter() {
    return new CompositionGraphInterpretationService(formulationTanninLevelAnalyzer());
  }

  private static @NonNull OptimizeCompositionCommand command(
      @Nullable String tannin
  ) {
    return new OptimizeCompositionCommand(
        List.of(
            source("sweet", "1.060", "5.0", tannin),
            source("sharp", "1.040", "9.0", tannin)
        ),
        FormulationTanninPolicy.CALCULATE_WHEN_COMPLETE,
        new FormulationTargetDefinition(
            new FormulationTargetDefinition.CustomRectangleTarget(
                closed(SpecificGravityValue.of("1.049"), SpecificGravityValue.of("1.051")),
                closed(TitratableAcidityValue.of("5.9"), TitratableAcidityValue.of("7.1"))
            ),
            null,
            new FormulationTargetDefinition.ScoringConfiguration(
                FormulationTargetDefinition.Normalizer.EFFECTIVE_RANGE,
                FormulationTargetDefinition.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
                new FormulationTargetDefinition.Weights(BigDecimal.ONE, BigDecimal.ONE, null)
            )
        ),
        List.of(),
        new CompositionOptimizationSettings(50, 3),
        CompositionOptimizationMode.PROPORTION,
        CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH,
        FormulationCalculator.MASS_BALANCE,
        null
    );
  }

  private static @NonNull OptimizationSourceInput source(
      @NonNull String id,
      @NonNull String specificGravity,
      @NonNull String titratableAcidity,
      @Nullable String tannin
  ) {
    return new OptimizationSourceInput(
        new CompositionSourceInput.Reference("FEEDSTOCK_BATCH", id),
        new CompositionSourceInput.Characteristics(
            org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue.of(
                SpecificGravityValue.of(specificGravity)
            ),
            org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue.of(
                TitratableAcidityValue.of(titratableAcidity)
            ),
            tannin == null
                ? null
                : org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue.of(
                    org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue.of(tannin)
                ),
            null
        ),
        null
    );
  }

}
