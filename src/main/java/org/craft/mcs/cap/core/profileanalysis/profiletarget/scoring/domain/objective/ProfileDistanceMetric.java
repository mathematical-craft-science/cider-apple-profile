package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective;


public enum ProfileDistanceMetric {

  /**
   * Euclidean distance after axis normalization and per-axis weighting.
   */
  NORMALIZED_WEIGHTED_EUCLIDEAN,

  /**
   * Manhattan distance after axis normalization and per-axis weighting.
   */
  NORMALIZED_WEIGHTED_MANHATTAN
}
