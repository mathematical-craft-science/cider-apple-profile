package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanTanninLevel;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.BuiltInCompositionCalculatorSelector;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSourceReference;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.usecase.OptimizeCompositionService;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationSource;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.BuiltInCompositionOptimizationStrategySelector;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.service.CompositionGraphInterpretationService;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.BuiltInProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.BuiltInProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.CompositionSourceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto.CompositionOptimizationCandidateResponse;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange.closed;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .compositionSourceCharacteristics;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .northAmericanProfileCalculator;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.predictor;


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
        new BuiltInProfileAxisNormalizerSelector(),
        new BuiltInProfileTargetObjectiveFunctionFactory(),
        predictor()
    );
  }

  private static @NonNull CompositionGraphInterpretationService graphInterpreter() {
    return new CompositionGraphInterpretationService(northAmericanProfileCalculator());
  }

  private static @NonNull OptimizeCompositionCommand command(
      @Nullable String tannin
  ) {
    return new OptimizeCompositionCommand(
        List.of(
            source("sweet", "1.060", "5.0", tannin),
            source("sharp", "1.040", "9.0", tannin)
        ),
        CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
        ProfileTargetObjective.of(new CustomRectangleSugarAcidityTarget(
            new RectangleSugarAcidityTargetGeometry(
                closed(
                    SpecificGravityValue.of("1.049"),
                    SpecificGravityValue.of("1.051")
                ),
                closed(
                    TitratableAcidityValue.of("5.9"),
                    TitratableAcidityValue.of("7.1")
                )
            )
        )),
        new ProfileTargetScoringConfiguration(
            ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
            ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
            ProfileTargetObjectiveWeights.balanced()
        ),
        List.of(),
        new DiscretizedCompositionOptimizationSettings(50, 3),
        CompositionOptimizationMode.PROPORTION,
        CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH,
        CompositionCalculatorCode.MASS_BALANCE,
        null
    );
  }

  private static @NonNull CompositionOptimizationSource source(
      @NonNull String id,
      @NonNull String specificGravity,
      @NonNull String titratableAcidity,
      @Nullable String tannin
  ) {
    return new CompositionOptimizationSource(
        new CompositionSourceReference("FEEDSTOCK_BATCH", id),
        compositionSourceCharacteristics(specificGravity, titratableAcidity, tannin, null),
        null
    );
  }

}
