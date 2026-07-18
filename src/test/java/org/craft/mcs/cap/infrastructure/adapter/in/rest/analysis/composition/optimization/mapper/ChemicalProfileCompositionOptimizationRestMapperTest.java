package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.mapper;

import java.math.BigDecimal;
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
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.BuiltInProfileAxisNormalizerSelector;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.BuiltInProfileTargetObjectiveFunctionFactory;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
                .isEqualByComparingTo(candidate.profile().sugar().value().value()))
            .satisfies(prediction -> assertThat(prediction.potentialAbv().unit()).isEqualTo("% ABV")));
  }

  private static @NonNull OptimizeChemicalProfileCompositionService service() {
    return new OptimizeChemicalProfileCompositionService(
        new BuiltInCompositionOptimizationStrategySelector(),
        new BuiltInChemicalProfileCompositionCalculatorSelector(),
        new BuiltInProfileAxisNormalizerSelector(BuiltInSugarAcidityZoneProfiles.thresholds()),
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
                new BigDecimal("1.049"),
                new BigDecimal("1.051"),
                new BigDecimal("5.9"),
                new BigDecimal("7.1")
            )
        )),
        ProfileTargetObjectiveWeights.balanced(),
        ProfileTargetScoringPurpose.BLENDING_PLANNING,
        List.of(),
        new DiscretizedCompositionOptimizationSettings(50, 3),
        CompositionOptimizationMode.PROPORTION,
        CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH,
        ChemicalProfileCompositionCalculatorCode.MASS_BALANCE,
        ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
        ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
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
        ChemicalProfileAxes.of(
            axis(ChemicalAxis.SUGAR, sugar),
            axis(ChemicalAxis.ACIDITY, acidity),
            null
        ),
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
}
