package org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in;


public enum ProfileTargetScoringPurpose {

  /**
   * Rank points for visual exploration and graph-adjacent similarity.
   */
  GRAPH_EXPLORATION,

  /**
   * Rank feedstock candidates for practical blending decisions.
   */
  BLENDING_PLANNING,

  /**
   * Rank candidates by closeness to built-in or custom target zones.
   */
  TARGET_ZONE_MATCHING,

  /**
   * Advanced calibration mode for technologist-tuned sensitivity units.
   */
  EXPERT_CALIBRATION
}
