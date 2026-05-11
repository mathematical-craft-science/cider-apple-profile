package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;


public enum BlendProfileAxisNormalizerCode {

  /**
   * Linear min-max scaling by full graph/domain ranges.
   */
  MIN_MAX,

  /**
   * Linear scaling by practical cider blending ranges.
   */
  EFFECTIVE_RANGE,

  /**
   * Piecewise scaling by sugar/acidity/tannin classification thresholds.
   */
  THRESHOLD_SEGMENT,

  /**
   * Scaling by expert sensitivity units for meaningful axis deltas.
   */
  SENSITIVITY_UNIT
}
