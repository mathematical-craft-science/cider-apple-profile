package org.craft.mcs.cap.core.formulation.interpretation.application.port.in;

import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculateCompositionResult;
import org.craft.mcs.cap.core.formulation.optimization.application.contract.OptimizeCompositionResult;
import org.craft.mcs.cap.core.formulation.interpretation.application.contract.CalculatedCompositionGraphView;
import org.craft.mcs.cap.core.formulation.interpretation.application.contract.CompositionOptimizationGraphView;
import org.jspecify.annotations.NonNull;

/** Enriches calculated compositions with graph-specific interpretation. */
public interface CompositionGraphInterpretationUseCase {

  @NonNull CalculatedCompositionGraphView interpret(
      @NonNull CalculateCompositionResult calculation
  );

  @NonNull CompositionOptimizationGraphView interpret(
      @NonNull OptimizeCompositionResult optimization
  );
}
