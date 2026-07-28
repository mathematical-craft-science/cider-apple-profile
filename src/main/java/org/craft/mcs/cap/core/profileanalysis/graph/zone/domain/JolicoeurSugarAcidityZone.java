package org.craft.mcs.cap.core.profileanalysis.graph.zone.domain;


/**
 * Zone vocabulary of the Jolicoeur sugar-acidity graph interpretation.
 *
 * <p>The values are Jolicoeur interpretations, not canonical SG or TA levels.</p>
 */
public enum JolicoeurSugarAcidityZone {

  /** SG and TA are both inside the configured central zone. */
  PERFECT_APPLES,

  /** Sugar estimate is above the configured central range. */
  HIGH_SUGAR_APPLES,

  /** Technological acidity is below the configured central range. */
  LOW_ACIDITY_APPLES,

  /** Sugar estimate is in the configured middle range outside the central zone. */
  MEDIUM_SUGAR_APPLES,

  /** Sugar estimate is below the configured middle range. */
  LOW_SUGAR_APPLES
}
