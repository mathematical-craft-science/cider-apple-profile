package org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.model.ChemicalProfileCompositionOptimizationCandidateView;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record OptimizeChemicalProfileCompositionResult(
    @NonNull CompositionOptimizationMode mode,
    @NonNull CompositionOptimizationAlgorithm algorithm,
    @NonNull ChemicalProfileCompositionCalculatorCode calculatorCode,
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunctionCode,
    int evaluatedCandidates,
    @NonNull List<@NonNull ChemicalProfileCompositionOptimizationCandidateView> candidates
) {

  public OptimizeChemicalProfileCompositionResult {
    requireNonNull(mode, "composition optimization mode cannot be null");
    requireNonNull(algorithm, "composition optimization algorithm cannot be null");
    requireNonNull(calculatorCode, "chemical profile composition calculator code cannot be null");
    requireNonNull(normalizerCode, "profile axis normalizer code cannot be null");
    requireNonNull(objectiveFunctionCode, "profile target objective function code cannot be null");
    if (evaluatedCandidates < 0) {
      throw new ApplicationValidationException("evaluated composition candidate count cannot be negative");
    }
    candidates = List.copyOf(requireNonNull(
        candidates,
        "composition optimization candidate views cannot be null"
    ));
  }
}
