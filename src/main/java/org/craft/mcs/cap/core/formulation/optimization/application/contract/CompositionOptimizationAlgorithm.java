package org.craft.mcs.cap.core.formulation.optimization.application.contract;


/** Search algorithm requested for composition optimization. */
public enum CompositionOptimizationAlgorithm {
  DISCRETIZED_BOUNDED_SEARCH,
  LINEAR_PROGRAMMING,
  QUADRATIC_PROGRAMMING,
  MIXED_INTEGER_PROGRAMMING,
  LOCAL_SEARCH,
  EVOLUTIONARY_HEURISTIC,
  NONLINEAR_OPTIMIZATION
}
