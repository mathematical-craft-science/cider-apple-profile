package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective;


/**
 * Defines how an optional optimization criterion behaves when a candidate cannot provide the
 * required calculated value.
 */
public enum CompositionOptimizationMissingPolicy {
  IGNORE_IF_MISSING,
  PENALIZE_IF_MISSING,
  EXCLUDE_IF_MISSING
}
