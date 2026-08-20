package org.craft.mcs.cap.core.formulation.optimization.domain;

import org.craft.mcs.cap.core.formulation.calculation.domain.CalculatedComposition;
import org.jspecify.annotations.NonNull;
import static java.util.Objects.requireNonNull;


/**
 * One optimized candidate returned by an optimization strategy.
 *
 * <p>Target fit, production-readiness penalties and ranking are added by application orchestration
 * after this pure bounded-search candidate has been generated.
 */
public record CompositionOptimizationCandidate(@NonNull CalculatedComposition composition) {

  public CompositionOptimizationCandidate {
    requireNonNull(composition, "composition cannot be null");
  }
}
