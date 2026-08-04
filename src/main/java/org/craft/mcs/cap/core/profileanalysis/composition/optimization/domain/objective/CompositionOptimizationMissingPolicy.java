package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective;


/**
 * Defines how an optional optimization criterion behaves when a candidate cannot provide the
 * required calculated value.
 */
public enum CompositionOptimizationMissingPolicy {

  /** Omits the criterion when its required characteristic cannot be calculated. */
  IGNORE_IF_MISSING,

  /** Keeps the candidate but applies a missing-data penalty. */
  PENALIZE_IF_MISSING,

  /** Excludes the candidate when its required characteristic cannot be calculated. */
  EXCLUDE_IF_MISSING
}
