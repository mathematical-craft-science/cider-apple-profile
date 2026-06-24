package org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in;

import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.ChemicalProfileCompositionOptimizationResult;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
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
