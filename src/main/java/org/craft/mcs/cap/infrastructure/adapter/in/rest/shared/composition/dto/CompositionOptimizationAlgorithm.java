package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto;


/** Transport code of a composition optimization algorithm. */
public enum CompositionOptimizationAlgorithm {
  DISCRETIZED_BOUNDED_SEARCH,
  LINEAR_PROGRAMMING,
  QUADRATIC_PROGRAMMING,
  MIXED_INTEGER_PROGRAMMING,
  LOCAL_SEARCH,
  EVOLUTIONARY_HEURISTIC,
  NONLINEAR_OPTIMIZATION
}
