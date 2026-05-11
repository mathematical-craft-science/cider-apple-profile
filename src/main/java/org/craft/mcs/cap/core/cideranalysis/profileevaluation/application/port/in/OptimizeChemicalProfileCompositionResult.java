package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.ChemicalProfileCompositionOptimizationResult;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.CompositionOptimizationMode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjectiveFunctionCode;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record OptimizeChemicalProfileCompositionResult(
    @NonNull CompositionOptimizationMode mode,
    @NonNull CompositionOptimizationAlgorithm algorithm,
    @NonNull ChemicalProfileCompositionCalculatorCode calculatorCode,
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunctionCode,
    @NonNull ChemicalProfileCompositionOptimizationResult optimizationResult
) {

  public OptimizeChemicalProfileCompositionResult {
    requireNonNull(mode, "composition optimization mode cannot be null");
    requireNonNull(algorithm, "composition optimization algorithm cannot be null");
    requireNonNull(calculatorCode, "chemical profile composition calculator code cannot be null");
    requireNonNull(normalizerCode, "profile axis normalizer code cannot be null");
    requireNonNull(objectiveFunctionCode, "profile target objective function code cannot be null");
    requireNonNull(optimizationResult, "chemical profile composition optimization result cannot be null");
  }
}
