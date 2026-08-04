package org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.port.in;

import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateCompositionResult;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeCompositionResult;
import org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.model.CalculatedCompositionGraphView;
import org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.model.CompositionOptimizationGraphView;
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
