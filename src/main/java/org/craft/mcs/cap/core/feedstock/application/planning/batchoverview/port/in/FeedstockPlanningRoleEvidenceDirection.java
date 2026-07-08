package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in;


/**
 * Direction of a source axis value relative to the selected target or target coordinate.
 */
public enum FeedstockPlanningRoleEvidenceDirection {

  /**
   * Source value is below the target coordinate and can soften or dilute that axis in a blend.
   */
  BELOW_TARGET_OR_BLEND,

  /**
   * Source value is close enough to the target coordinate for this evidence fact.
   */
  ON_TARGET_OR_BLEND,

  /**
   * Source value is above the target coordinate and can raise that axis in a blend.
   */
  ABOVE_TARGET_OR_BLEND
}
