package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.model;


/**
 * User-facing analysis scenario used by the application layer to select scoring defaults.
 *
 * <p>The scenario is not a mathematical scoring parameter. It must be resolved to an explicit
 * scoring configuration before profile-target distance is calculated.
 */
public enum ProfileTargetAnalysisScenario {

  /** Broad visual comparison of profile points on a graph. */
  GRAPH_EXPLORATION,

  /** Assessment of source or composition candidates during blend planning. */
  BLENDING_PLANNING,

  /** Comparison focused on membership or proximity to a selected target zone. */
  TARGET_ZONE_MATCHING
}
