package org.craft.mcs.cap.core.formulation.optimization.application.contract;

import java.util.List;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationCalculator;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition.Normalizer;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition.ObjectiveFunction;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Application result for one optimization run, including resolved strategy metadata and ranked candidates. */
public record OptimizeCompositionResult(
    @NonNull CompositionOptimizationMode mode,
    @NonNull CompositionOptimizationAlgorithm algorithm,
    @NonNull FormulationCalculator calculator,
    @NonNull Normalizer normalizer,
    @NonNull ObjectiveFunction objectiveFunction,
    @NonNull CompositionOptimizationSettings settings,
    int evaluatedCandidates,
    @NonNull List<@NonNull CompositionOptimizationCandidateView> candidates
) {

  public OptimizeCompositionResult {
    requireNonNull(mode, "composition optimization mode cannot be null");
    requireNonNull(algorithm, "composition optimization algorithm cannot be null");
    requireNonNull(calculator, "composition calculator cannot be null");
    requireNonNull(normalizer, "profile axis normalizer cannot be null");
    requireNonNull(objectiveFunction, "profile target objective function cannot be null");
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
    if (candidates.size() > settings.limit()) {
      throw new ApplicationValidationException(
          "returned composition candidate views cannot exceed the requested result limit"
      );
    }
  }
}
