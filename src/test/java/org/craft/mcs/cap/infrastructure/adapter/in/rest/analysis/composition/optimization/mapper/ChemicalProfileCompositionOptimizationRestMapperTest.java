package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.mapper;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.BuiltInChemicalProfileCompositionCalculatorSelector;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSourceReference;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.usecase.OptimizeChemicalProfileCompositionService;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.ChemicalProfileCompositionOptimizationSource;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.BuiltInCompositionOptimizationStrategySelector;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.BuiltInProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.BuiltInProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange.closed;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .compositionSourceCharacteristics;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.predictor;


class ChemicalProfileCompositionOptimizationRestMapperTest {

  @Test
  void mapsPotentialAbvForEveryOptimizationCandidate() {
    final var result = service().optimize(command());

    final var response = ChemicalProfileCompositionOptimizationRestMapper.toResponse(result, 50);

    assertThat(response.candidates())
        .isNotEmpty()
        .allSatisfy(candidate -> assertThat(candidate.potentialAbv())
            .isNotNull()
            .satisfies(prediction -> assertThat(prediction.originalGravity().value())
                .isEqualByComparingTo(candidate.characteristics().specificGravity().value()))
            .satisfies(prediction -> assertThat(prediction.potentialAbv().unit()).isEqualTo("% ABV")));
  }

  private static @NonNull OptimizeChemicalProfileCompositionService service() {
    return new OptimizeChemicalProfileCompositionService(
        new BuiltInCompositionOptimizationStrategySelector(),
        new BuiltInChemicalProfileCompositionCalculatorSelector(),
        new BuiltInProfileAxisNormalizerSelector(),
        new BuiltInProfileTargetObjectiveFunctionFactory(),
        predictor()
    );
  }

  private static @NonNull OptimizeChemicalProfileCompositionCommand command() {
    return new OptimizeChemicalProfileCompositionCommand(
        List.of(
            source("sweet", "1.060", "5.0"),
            source("sharp", "1.040", "9.0")
        ),
        ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE,
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
        ChemicalProfileCompositionCalculatorCode.MASS_BALANCE,
        null
    );
  }

  private static @NonNull ChemicalProfileCompositionOptimizationSource source(
      @NonNull String id,
      @NonNull String sugar,
      @NonNull String acidity
  ) {
    return new ChemicalProfileCompositionOptimizationSource(
        new ChemicalProfileCompositionSourceReference("FEEDSTOCK_BATCH", id),
        compositionSourceCharacteristics(sugar, acidity, null, null),
        null
    );
  }

}
