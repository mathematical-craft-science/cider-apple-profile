package org.craft.mcs.cap.core.formulation.optimization.application.usecase;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CompositionSourceInput;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationCalculator;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationPotentialAbvView;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTanninPolicy;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDistanceView;
import org.craft.mcs.cap.core.formulation.calculation.application.port.out.FormulationTargetFitAnalyzer;
import org.craft.mcs.cap.core.formulation.calculation.domain.CalculatedComposition;
import org.craft.mcs.cap.core.formulation.calculation.domain.CalculatedCompositionCharacteristics;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionCalculator;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionProportion;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionSource;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionSourceCharacteristics;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionSourceReference;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationMode;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationSettings;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationCriterionInput;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationCriterionInput.MissingPolicy;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationCriterionInput.YanReadiness;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationObjectiveView;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationSourceInput;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizeCompositionResult;
import org.craft.mcs.cap.core.formulation.optimization.application.port.in.OptimizeCompositionCommand;
import org.craft.mcs.cap.core.formulation.optimization.domain.CompositionOptimizationCandidate;
import org.craft.mcs.cap.core.formulation.optimization.domain.CompositionOptimizationResult;
import org.craft.mcs.cap.core.formulation.optimization.domain.strategy.CompositionOptimizationStrategy;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class OptimizeCompositionServiceTest {

  @Test
  void ranksActualTargetMemberBeforeExcludedBoundaryWithSameObjectiveScore() {
    final var strategy = mock(CompositionOptimizationStrategy.class);
    final var calculator = mock(CompositionCalculator.class);
    when(strategy.optimize(any(), any())).thenReturn(new CompositionOptimizationResult(
        2,
        List.of(
            new CompositionOptimizationCandidate(composition("1.045")),
            new CompositionOptimizationCandidate(composition("1.044"))
        )
    ));
    final var service = new OptimizeCompositionService(
        ignored -> strategy,
        ignored -> calculator,
        (characteristics, ignored) -> targetDistance(characteristics.specificGravity()),
        OptimizeCompositionServiceTest::potentialAbv
    );

    final var result = service.optimize(command(List.of()));

    assertThat(result.candidates())
        .extracting(candidate -> candidate.composition().characteristics().specificGravity())
        .containsExactly(SpecificGravityValue.of("1.044"), SpecificGravityValue.of("1.045"));
    assertThat(result.candidates())
        .extracting(candidate -> candidate.targetDistance().insideTarget())
        .containsExactly(true, false);
  }

  @Test
  void ranksCandidateWithoutYanPenaltyBeforeCloserLowYanCandidate() {
    final var strategy = strategyReturning(
        composition("1.044", "60"),
        composition("1.045", "200")
    );
    final var service = service(
        strategy,
        (characteristics, ignored) -> targetDistance(
            characteristics.specificGravity(),
            characteristics.specificGravity().equals(SpecificGravityValue.of("1.044"))
                ? BigDecimal.ZERO
                : new BigDecimal("0.1"),
            false
        )
    );

    final var result = service.optimize(command(List.of(yanCriterion(
        MissingPolicy.PENALIZE_IF_MISSING
    ))));

    assertThat(result.candidates())
        .extracting(candidate -> candidate.composition().characteristics().yeastAssimilableNitrogen())
        .containsExactly(
            YeastAssimilableNitrogenValue.of("200"),
            YeastAssimilableNitrogenValue.of("60")
        );
    assertThat(result.candidates().getLast().objective().penalties())
        .extracting(OptimizationObjectiveView.Penalty::code)
        .containsExactly(OptimizationObjectiveView.Code.LOW_YEAST_ASSIMILABLE_NITROGEN);
  }

  @Test
  void excludesCandidateWithMissingYanWhenCriterionRequiresIt() {
    final var strategy = strategyReturning(composition("1.044", null));
    final var service = service(
        strategy,
        (characteristics, ignored) -> targetDistance(
            characteristics.specificGravity(),
            BigDecimal.ZERO,
            true
        )
    );

    final var result = service.optimize(command(List.of(yanCriterion(
        MissingPolicy.EXCLUDE_IF_MISSING
    ))));

    assertThat(result)
        .returns(1, OptimizeCompositionResult::evaluatedCandidates)
        .satisfies(value -> assertThat(value.candidates()).isEmpty());
  }

  private static @NonNull OptimizeCompositionCommand command(
      @NonNull List<@NonNull OptimizationCriterionInput> criteria
  ) {
    return new OptimizeCompositionCommand(
        List.of(sourceInput("source-a"), sourceInput("source-b")),
        FormulationTanninPolicy.CALCULATE_WHEN_COMPLETE,
        new FormulationTargetDefinition(
            new FormulationTargetDefinition.PredefinedTarget(
                FormulationTargetDefinition.Zone.LOW_SUGAR_APPLES
            ),
            null,
            new FormulationTargetDefinition.ScoringConfiguration(
                FormulationTargetDefinition.Normalizer.MIN_MAX,
                FormulationTargetDefinition.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
                new FormulationTargetDefinition.Weights(BigDecimal.ONE, BigDecimal.ONE, null)
            )
        ),
        criteria,
        new CompositionOptimizationSettings(50, 2),
        CompositionOptimizationMode.PROPORTION,
        CompositionOptimizationAlgorithm.DISCRETIZED_BOUNDED_SEARCH,
        FormulationCalculator.MASS_BALANCE,
        null
    );
  }

  private static @NonNull YanReadiness yanCriterion(
      @NonNull MissingPolicy missingPolicy
  ) {
    return new YanReadiness(
        YeastAssimilableNitrogenValue.of("140"),
        null,
        new BigDecimal("10"),
        missingPolicy,
        BigDecimal.ONE
    );
  }

  private static @NonNull CompositionOptimizationStrategy strategyReturning(
      @NonNull CalculatedComposition... compositions
  ) {
    final var strategy = mock(CompositionOptimizationStrategy.class);
    when(strategy.optimize(any(), any())).thenReturn(new CompositionOptimizationResult(
        compositions.length,
        Arrays.stream(compositions)
            .map(CompositionOptimizationCandidate::new)
            .toList()
    ));
    return strategy;
  }

  private static @NonNull OptimizeCompositionService service(
      @NonNull CompositionOptimizationStrategy strategy,
      @NonNull FormulationTargetFitAnalyzer targetFitAnalyzer
  ) {
    final var calculator = mock(CompositionCalculator.class);
    return new OptimizeCompositionService(
        ignored -> strategy,
        ignored -> calculator,
        targetFitAnalyzer,
        OptimizeCompositionServiceTest::potentialAbv
    );
  }

  private static @NonNull OptimizationSourceInput sourceInput(@NonNull String id) {
    return new OptimizationSourceInput(
        new CompositionSourceInput.Reference("test", id),
        new CompositionSourceInput.Characteristics(
            PointCharacteristicValue.of(SpecificGravityValue.of("1.044")),
            PointCharacteristicValue.of(TitratableAcidityValue.of("7.6")),
            null,
            null
        ),
        null
    );
  }

  private static @NonNull CalculatedComposition composition(@NonNull String specificGravity) {
    return composition(specificGravity, null);
  }

  private static @NonNull CalculatedComposition composition(
      @NonNull String specificGravity,
      @Nullable String yeastAssimilableNitrogen
  ) {
    return CalculatedComposition.of(
        List.of(source("source-a"), source("source-b")),
        new CalculatedCompositionCharacteristics(
            SpecificGravityValue.of(specificGravity),
            TitratableAcidityValue.of("7.6"),
            null,
            yeastAssimilableNitrogen == null
                ? null
                : YeastAssimilableNitrogenValue.of(yeastAssimilableNitrogen)
        ),
        List.of()
    );
  }

  private static @NonNull CompositionSource source(@NonNull String id) {
    return new CompositionSource(
        new CompositionSourceReference("test", id),
        new CompositionSourceCharacteristics(
            PointCharacteristicValue.of(SpecificGravityValue.of("1.044")),
            PointCharacteristicValue.of(TitratableAcidityValue.of("7.6")),
            null,
            null
        ),
        CompositionProportion.of("0.5"),
        null
    );
  }

  private static @NonNull FormulationTargetDistanceView targetDistance(
      @NonNull SpecificGravityValue specificGravity
  ) {
    final var inside = specificGravity.equals(SpecificGravityValue.of("1.044"));
    return targetDistance(specificGravity, BigDecimal.ZERO, inside);
  }

  private static @NonNull FormulationTargetDistanceView targetDistance(
      @NonNull SpecificGravityValue specificGravity,
      @NonNull BigDecimal distance,
      boolean inside
  ) {
    final var coordinates = new FormulationTargetDistanceView.Coordinates(
        specificGravity,
        TitratableAcidityValue.of("7.6")
    );
    return new FormulationTargetDistanceView(
        FormulationTargetDistanceView.Status.SCORED,
        coordinates,
        coordinates,
        new FormulationTargetDistanceView.TargetPosition(
            inside
                ? FormulationTargetDistanceView.AxisPosition.WITHIN
                : FormulationTargetDistanceView.AxisPosition.ABOVE,
            FormulationTargetDistanceView.AxisPosition.WITHIN
        ),
        null,
        null,
        distance,
        100,
        FormulationTargetDistanceView.FitStatus.EXCELLENT,
        inside,
        inside,
        FormulationTargetDefinition.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
        FormulationTargetDistanceView.Metric.NORMALIZED_WEIGHTED_EUCLIDEAN,
        new FormulationTargetDefinition.Weights(BigDecimal.ONE, BigDecimal.ONE, null),
        FormulationTargetDistanceView.TargetKind.PREDEFINED_ZONE,
        FormulationTargetDistanceView.GeometryShape.RECTANGLE,
        Set.of()
    );
  }

  private static @NonNull FormulationPotentialAbvView potentialAbv(
      @NonNull SpecificGravityValue originalGravity
  ) {
    return new FormulationPotentialAbvView(
        originalGravity,
        SpecificGravityValue.of("1.000"),
        FormulationPotentialAbvView.FinalGravitySource.FIXED_DRY_CIDER_DEFAULT,
        AbvValue.of(5.8),
        FormulationPotentialAbvView.Method.STANDARD_GRAVITY_DROP,
        FormulationPotentialAbvView.Confidence.MEDIUM
    );
  }
}
