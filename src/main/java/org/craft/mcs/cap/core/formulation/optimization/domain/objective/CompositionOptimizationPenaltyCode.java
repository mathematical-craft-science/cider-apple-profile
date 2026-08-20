package org.craft.mcs.cap.core.formulation.optimization.domain.objective;


/** Stable code explaining why an optional criterion penalized an optimization candidate. */
public enum CompositionOptimizationPenaltyCode {

  /** Candidate YAN could not be calculated from the selected sources. */
  MISSING_YEAST_ASSIMILABLE_NITROGEN,

  /** Calculated candidate YAN is below the configured readiness range. */
  LOW_YEAST_ASSIMILABLE_NITROGEN,

  /** Calculated candidate YAN is above the configured readiness range. */
  HIGH_YEAST_ASSIMILABLE_NITROGEN
}
