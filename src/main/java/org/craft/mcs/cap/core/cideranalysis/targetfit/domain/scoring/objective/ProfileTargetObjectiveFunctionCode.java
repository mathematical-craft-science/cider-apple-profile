package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.objective;


/**
 * Stable codes for selecting the target objective/distance function.
 *
 * <p>The objective function determines how normalized deviations are aggregated. L2/Euclidean
 * rewards candidates near the center-like nearest point and penalizes large errors more strongly.
 * L1/Manhattan adds deviations linearly and can be easier to explain for operational blending
 * recommendations.
 */
public enum ProfileTargetObjectiveFunctionCode {

  /**
   * Distance is zero inside target area; outside it is measured to the nearest target boundary point.
   */
  DISTANCE_TO_TARGET_AREA,

  /**
   * L1 variant: distance is zero inside target area; outside it is measured as normalized weighted Manhattan distance.
   */
  MANHATTAN_DISTANCE_TO_TARGET_AREA
}
