package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;


public enum BlendProfileTargetDistanceStatus {

  /**
   * All measurements required by the objective were present and distance was calculated.
   */
  SCORED,

  /**
   * Distance could not be calculated because the objective requires absent measurements.
   */
  UNSCORABLE
}
