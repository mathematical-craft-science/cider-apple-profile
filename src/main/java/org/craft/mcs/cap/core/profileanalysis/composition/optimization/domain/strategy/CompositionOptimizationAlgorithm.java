package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy;

import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;


/**
 * Stable codes for selecting the algorithm that proposes candidate source weights.
 *
 * <p>The algorithm is only one part of optimization. A complete optimization run also selects:
 * <ul>
 *   <li>a {@link CompositionCalculatorCode}
 *       to calculate each candidate profile;</li>
 *   <li>a {@link ProfileAxisNormalizerCode}
 *       to normalize axes before distance calculation;</li>
 *   <li>a {@link ProfileTargetObjectiveFunctionCode}
 *       to score candidate distance to the target.</li>
 * </ul>
 */
public enum CompositionOptimizationAlgorithm {

  /**
   * Enumerates bounded discrete source shares. Explainable MVP algorithm for small source sets.
   */
  DISCRETIZED_BOUNDED_SEARCH,

  /**
   * Future algorithm for linear objectives and linear constraints.
   *
   * <p>Useful when the objective can be represented as a linear expression, for example by
   * minimizing absolute deviations through auxiliary variables. This is a strong candidate for
   * explaining blending recommendations with simple constraints.
   */
  LINEAR_PROGRAMMING,

  /**
   * Future algorithm for quadratic objectives and linear constraints.
   *
   * <p>Useful for normalized L2 distance to a target point or rectangle when the profile calculator
   * remains linear but the objective squares deviations.
   */
  QUADRATIC_PROGRAMMING,

  /**
   * Future algorithm for integer or semi-discrete production constraints.
   *
   * <p>Useful when a cider maker can only use whole containers, pumpable increments, minimum batch
   * draw volumes, or a bounded number of source batches.
   */
  MIXED_INTEGER_PROGRAMMING,

  /**
   * Future local improvement algorithm that starts from a feasible candidate and iteratively
   * adjusts proportions.
   *
   * <p>Useful as a faster refinement step after coarse grid search, or when the source count makes
   * exhaustive enumeration too expensive.
   */
  LOCAL_SEARCH,

  /**
   * Future evolutionary or genetic-style heuristic for complex, non-convex objectives.
   *
   * <p>Useful when the objective includes sensory penalties, preference models, nonlinear
   * constraints, or other criteria that are hard to express in linear/quadratic solver form.
   */
  EVOLUTIONARY_HEURISTIC,

  /**
   * Future algorithm for L2, ellipses, nonlinear calculators and nonlinear quality functions.
   *
   * <p>This is the broad fallback category for solver-backed nonlinear optimization once the domain
   * model grows beyond the simple mass-balance MVP.
   */
  NONLINEAR_OPTIMIZATION
}
