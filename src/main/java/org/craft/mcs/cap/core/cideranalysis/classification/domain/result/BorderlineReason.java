package org.craft.mcs.cap.core.cideranalysis.classification.domain.result;

/**
 * Explains why a matched profile classification is considered borderline and receives reduced confidence.
 *
 * <p>A reason is diagnostic evidence about the matched classification rule, not a separate classification
 * result or a user-facing severity.</p>
 */
public enum BorderlineReason {

  /**
   * The representative axis value lies within the configured margin around a classification boundary.
   */
  CLOSE_TO_BOUNDARY,

  /**
   * The measured interval extends beyond at least one boundary of the matched classification range.
   */
  RANGE_OVERLAPS_BOUNDARY,

  /**
   * The profile has no value for an axis required to assess the matched classification rule confidently.
   */
  MISSING_AXIS
}
