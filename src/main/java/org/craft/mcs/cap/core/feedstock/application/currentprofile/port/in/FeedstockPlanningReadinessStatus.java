package org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in;


/**
 * Aggregated planning-readiness status for using a batch in planning.
 */
public enum FeedstockPlanningReadinessStatus {

  /**
   * The batch can be used as a candidate planning source.
   */
  READY,

  /**
   * The batch can be considered, but has warnings that should be reviewed before confirmation.
   */
  NEEDS_ATTENTION,

  /**
   * The batch lacks mandatory planning conditions and should not be selectable for confirmation.
   */
  BLOCKED
}
