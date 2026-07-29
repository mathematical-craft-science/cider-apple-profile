package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective;


/** Effect of one optimization penalty on candidate interpretation and eligibility. */
public enum CompositionOptimizationPenaltySeverity {

  /** Informational trade-off that does not make the candidate less suitable. */
  INFO,

  /** Non-blocking risk that lowers the candidate objective score. */
  WARNING,

  /** Constraint violation that excludes the candidate from eligible results. */
  BLOCKING
}
