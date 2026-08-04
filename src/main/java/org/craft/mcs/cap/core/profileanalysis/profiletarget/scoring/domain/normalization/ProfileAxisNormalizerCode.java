package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization;


/**
 * Stable codes for selecting an axis normalization strategy.
 *
 * <p>Different use cases need different normalization semantics: graph exploration can use full
 * axis ranges, cider blending can use effective cider-making ranges, zone matching can emphasize
 * scoring-calibration segments, and expert calibration can use hand-tuned sensitivity units.
 */
public enum ProfileAxisNormalizerCode {

  /**
   * Linear min-max scaling by full graph/domain ranges.
   */
  MIN_MAX,

  /**
   * Linear scaling by practical cider blending ranges.
   */
  EFFECTIVE_RANGE,

  /**
   * Piecewise scaling by SG/TA/tannin scoring-calibration segments.
   */
  THRESHOLD_SEGMENT,

  /**
   * Scaling by expert sensitivity units for meaningful axis deltas.
   */
  SENSITIVITY_UNIT
}
