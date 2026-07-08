package org.craft.mcs.cap.core.profileanalysis.ph.domain;


/**
 * Stable code for pH planning guidance.
 *
 * <p>These codes are recommendations for production planning. They must not be interpreted as a
 * hard physical fermentation constraint.
 */
public enum PhPlanningGuidanceCode {

  /**
   * pH is inside the curated generic cider planning range.
   */
  PH_PLANNING_IDEAL,

  /**
   * pH is below the curated generic cider planning range and may require yeast/process planning.
   */
  PH_PLANNING_LOW,

  /**
   * pH is above the curated generic cider planning range and should trigger microbial-risk review.
   */
  PH_PLANNING_HIGH,

  /**
   * pH is absent, so pH-related production risks cannot be assessed.
   */
  PH_MISSING
}
