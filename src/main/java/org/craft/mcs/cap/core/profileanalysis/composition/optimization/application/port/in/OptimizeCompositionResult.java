package org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.model.CompositionOptimizationCandidateView;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Application result for one optimization run, including resolved strategy metadata and ranked candidates. */
public record OptimizeCompositionResult(
    @NonNull CompositionOptimizationMode mode,
    @NonNull CompositionOptimizationAlgorithm algorithm,
    @NonNull CompositionCalculatorCode calculatorCode,
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunctionCode,
    @NonNull DiscretizedCompositionOptimizationSettings settings,
    int evaluatedCandidates,
    @NonNull List<@NonNull CompositionOptimizationCandidateView> candidates
) {

  public OptimizeCompositionResult {
    requireNonNull(mode, "composition optimization mode cannot be null");
    requireNonNull(algorithm, "composition optimization algorithm cannot be null");
    requireNonNull(calculatorCode, "composition calculator code cannot be null");
    requireNonNull(normalizerCode, "profile axis normalizer code cannot be null");
    requireNonNull(objectiveFunctionCode, "profile target objective function code cannot be null");
    requireNonNull(settings, "composition optimization settings cannot be null");
    if (evaluatedCandidates < 0) {
      throw new ApplicationValidationException("evaluated composition candidate count cannot be negative");
    }
    candidates = List.copyOf(requireNonNull(
        candidates,
        "composition optimization candidate views cannot be null"
    ));
    if (candidates.size() > evaluatedCandidates) {
      throw new ApplicationValidationException(
          "returned composition candidate views cannot exceed evaluated candidate count"
      );
    }
  }
}
