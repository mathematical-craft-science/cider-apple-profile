package org.craft.mcs.cap.core.formulation.optimization.application.usecase;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.formulation.calculation.domain.InvalidCompositionException;
import org.craft.mcs.cap.core.formulation.calculation.domain.UnsupportedCompositionCalculatorException;
import org.craft.mcs.cap.core.formulation.optimization.application.InvalidCompositionOptimizationRequestException;
import org.craft.mcs.cap.core.formulation.optimization.application.UnsupportedCompositionOptimizationRequestException;
import org.craft.mcs.cap.core.formulation.calculation.application.port.out.FormulationAbvAnalyzer;
import org.craft.mcs.cap.core.formulation.calculation.application.port.out.FormulationTargetFitAnalyzer;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDistanceView;
import org.craft.mcs.cap.core.formulation.calculation.application.support.CalculatedCompositionViewMapper;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.CompositionOptimizationCandidateView;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationCriterionInput;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationObjectiveView;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizationSourceInput;
import org.craft.mcs.cap.core.formulation.optimization.application.port.in.OptimizeCompositionCommand;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizeCompositionResult;
import org.craft.mcs.cap.core.formulation.optimization.application.port.in.OptimizeCompositionUseCase;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionCalculatorCode;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionCalculatorSelector;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionSourceCharacteristics;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionSourceReference;
import org.craft.mcs.cap.core.formulation.calculation.domain.CompositionTanninPolicy;
import org.craft.mcs.cap.core.formulation.optimization.domain.CompositionOptimizationCandidate;
import org.craft.mcs.cap.core.formulation.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.formulation.optimization.domain.CompositionOptimizationRequest;
import org.craft.mcs.cap.core.formulation.optimization.domain.CompositionOptimizationSource;
import org.craft.mcs.cap.core.formulation.optimization.domain.InvalidCompositionOptimizationException;
import org.craft.mcs.cap.core.formulation.optimization.domain.objective.CompositionOptimizationCriterion;
import org.craft.mcs.cap.core.formulation.optimization.domain.objective.CompositionOptimizationMissingPolicy;
import org.craft.mcs.cap.core.formulation.optimization.domain.objective.CompositionOptimizationObjectiveAssessment;
import org.craft.mcs.cap.core.formulation.optimization.domain.objective.CompositionOptimizationObjectiveScorer;
import org.craft.mcs.cap.core.formulation.optimization.domain.objective.CompositionOptimizationPenalty;
import org.craft.mcs.cap.core.formulation.optimization.domain.objective.YeastAssimilableNitrogenReadinessCriterion;
import org.craft.mcs.cap.core.formulation.optimization.domain.strategy.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.formulation.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.formulation.optimization.domain.strategy.CompositionOptimizationStrategySelector;
import org.craft.mcs.cap.core.formulation.optimization.domain.strategy.UnsupportedCompositionOptimizationStrategyException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application orchestrator for composition optimization.
 *
 * <p>The service wires together independently selectable concerns:
 * <ol>
 *   <li>composition calculator, selected by {@code calculatorCode};</li>
 *   <li>optimization algorithm, selected by {@code algorithm};</li>
 *   <li>target-fit and ABV analysis through outbound ports;</li>
 *   <li>optional readiness penalties and final candidate ranking.</li>
 * </ol>
 *
 * <p>The orchestration lives in the application layer because it combines several domain
 * strategies for a concrete use case. The domain strategies remain independently testable and do
 * not know about REST, DTO defaults, or user-facing scenario names.
 */
@Component
@RequiredArgsConstructor
public final class OptimizeCompositionService
    implements OptimizeCompositionUseCase {

  private final @NonNull CompositionOptimizationStrategySelector optimizationStrategySelector;
  private final @NonNull CompositionCalculatorSelector calculatorSelector;
  private final @NonNull FormulationTargetFitAnalyzer targetFitAnalyzer;
  private final @NonNull FormulationAbvAnalyzer abvAnalyzer;

  @Override
  public @NonNull OptimizeCompositionResult optimize(
      @NonNull OptimizeCompositionCommand command
  ) {
    requireNonNull(command, "optimize composition command cannot be null");
    try {
      return optimizeValidated(command);
    } catch (InvalidCompositionException | InvalidCompositionOptimizationException exception) {
      throw new InvalidCompositionOptimizationRequestException(exception.getMessage(), exception);
    } catch (
        UnsupportedCompositionCalculatorException | UnsupportedCompositionOptimizationStrategyException exception
    ) {
      throw new UnsupportedCompositionOptimizationRequestException(exception.getMessage(), exception);
    }
  }

  private @NonNull OptimizeCompositionResult optimizeValidated(
      @NonNull OptimizeCompositionCommand command
  ) {
    final var calculator = calculatorSelector.select(CompositionCalculatorCode.valueOf(command.calculator().name()));
    final var optimizationStrategy = optimizationStrategySelector.select(
        CompositionOptimizationAlgorithm.valueOf(command.algorithm().name())
    );
    final var settings = new DiscretizedCompositionOptimizationSettings(
        command.settings().stepPercent(),
        command.settings().limit()
    );
    final var criteria = command.criteria().stream()
        .map(OptimizeCompositionService::toDomain)
        .toList();
    final var optimizationResult = optimizationStrategy.optimize(
        new CompositionOptimizationRequest(
            command.sources().stream().map(OptimizeCompositionService::toDomain).toList(),
            CompositionTanninPolicy.valueOf(command.tanninPolicy().name()),
            settings,
            CompositionOptimizationMode.valueOf(command.mode().name()),
            command.targetTotalVolume()
        ),
        calculator
    );

    final var rankedCandidates = optimizationResult.candidates().stream()
        .map(candidate -> toCandidateView(candidate, command, criteria))
        .flatMap(Optional::stream)
        .sorted(OptimizeCompositionService::compareCandidates)
        .limit(command.settings().limit())
        .toList();

    return new OptimizeCompositionResult(
        command.mode(),
        command.algorithm(),
        command.calculator(),
        command.target().scoring().normalizer(),
        command.target().scoring().objectiveFunction(),
        command.settings(),
        optimizationResult.evaluatedCandidates(),
        rankedCandidates
    );
  }

  private @NonNull Optional<@NonNull CompositionOptimizationCandidateView> toCandidateView(
      @NonNull CompositionOptimizationCandidate candidate,
      @NonNull OptimizeCompositionCommand command,
      @NonNull List<@NonNull CompositionOptimizationCriterion> criteria
  ) {
    final var composition = CalculatedCompositionViewMapper.toView(candidate.composition());
    final var targetDistance = targetFitAnalyzer.evaluate(composition.characteristics(), command.target());
    return CompositionOptimizationObjectiveScorer.assess(
            candidate.composition(),
            targetDistance.value(),
            criteria
        )
        .map(assessment -> new CompositionOptimizationCandidateView(
            composition,
            targetDistance,
            toView(assessment),
            abvAnalyzer.predict(composition.characteristics().specificGravity())
        ));
  }

  private static @NonNull CompositionOptimizationSource toDomain(@NonNull OptimizationSourceInput source) {
    return new CompositionOptimizationSource(
        new CompositionSourceReference(source.reference().type(), source.reference().id()),
        new CompositionSourceCharacteristics(
            source.characteristics().specificGravity(),
            source.characteristics().titratableAcidity(),
            source.characteristics().tannin(),
            source.characteristics().yeastAssimilableNitrogen()
        ),
        source.availableVolume()
    );
  }

  private static @NonNull CompositionOptimizationCriterion toDomain(
      @NonNull OptimizationCriterionInput criterion
  ) {
    return switch (criterion) {
      case OptimizationCriterionInput.YanReadiness(
          var minimum,
          var maximum,
          var weight,
          var missingPolicy,
          var missingPenalty
      ) -> new YeastAssimilableNitrogenReadinessCriterion( // NOSONAR
          minimum,
          maximum,
          weight,
          CompositionOptimizationMissingPolicy.valueOf(missingPolicy.name()),
          missingPenalty
      );
    };
  }

  private static @NonNull OptimizationObjectiveView toView(
      @NonNull CompositionOptimizationObjectiveAssessment assessment
  ) {
    return new OptimizationObjectiveView(
        assessment.score(),
        assessment.penalties().stream()
            .map(OptimizeCompositionService::toView)
            .toList()
    );
  }

  private static OptimizationObjectiveView.@NonNull Penalty toView(
      @NonNull CompositionOptimizationPenalty penalty
  ) {
    return new OptimizationObjectiveView.Penalty(
        OptimizationObjectiveView.Code.valueOf(penalty.code().name()),
        OptimizationObjectiveView.Severity.valueOf(penalty.severity().name()),
        penalty.value(),
        penalty.characteristic(),
        penalty.actualValue(),
        penalty.threshold()
    );
  }

  private static int compareCandidates(
      @NonNull CompositionOptimizationCandidateView left,
      @NonNull CompositionOptimizationCandidateView right
  ) {
    return Comparator
        .comparing(
            CompositionOptimizationCandidateView::objective,
            (leftObjective, rightObjective) -> compareNullable(
                leftObjective.score(),
                rightObjective.score()
            )
        )
        .thenComparing(
            CompositionOptimizationCandidateView::targetDistance,
            OptimizeCompositionService::compareTargetDistance
        )
        .compare(left, right);
  }

  private static int compareTargetDistance(
      @NonNull FormulationTargetDistanceView left,
      @NonNull FormulationTargetDistanceView right
  ) {
    final var valueComparison = compareNullable(left.value(), right.value());
    return valueComparison != 0
        ? valueComparison
        : Boolean.compare(right.insideTarget(), left.insideTarget());
  }

  private static int compareNullable(
      @Nullable BigDecimal left,
      @Nullable BigDecimal right
  ) {
    if (left == null) {
      return right == null ? 0 : 1;
    }
    return right == null ? -1 : left.compareTo(right);
  }
}
