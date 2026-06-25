package org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in;


/**
 * Stable machine-readable data-quality and evidence-shape indicator for the current feedstock profile.
 */
public enum FeedstockCurrentProfileDataQualityIndicatorCode {

  /**
   * The batch has no current measurement, so planning cannot use a reliable current chemical state.
   */
  CURRENT_MEASUREMENT_MISSING,

  /**
   * The current profile has enough sugar and titratable acidity evidence to be plotted on the
   * sugar-acidity graph.
   */
  SUGAR_ACIDITY_PLOTTABLE,

  /**
   * Sugar is missing from the current profile, so sugar-acidity plotting and composition planning are blocked.
   */
  SUGAR_MISSING,

  /**
   * Titratable acidity is missing from the current profile, so sugar-acidity plotting and composition planning
   * are blocked.
   */
  TITRATABLE_ACIDITY_MISSING,

  /**
   * Tannin is missing from the current profile, so target-aware structure assessment is incomplete.
   */
  TANNIN_MISSING,

  /**
   * pH is missing from the current profile; this does not block composition planning, but weakens fermentation
   * and safety assessment.
   */
  PH_MISSING,

  /**
   * YAN is missing from the current profile; this does not affect target distance, but weakens fermentation-start
   * readiness assessment.
   */
  YAN_MISSING,

  /**
   * The current measurement is old enough that the profile may no longer describe the current feedstock state.
   */
  MEASUREMENT_STALE,

  /**
   * Measurement provenance/source is missing, so the user cannot understand where the chemical data came from.
   */
  SOURCE_MISSING,

  /**
   * Measurement confidence is missing, so the user cannot judge trust level explicitly.
   */
  CONFIDENCE_MISSING,

  /**
   * Measurement confidence is explicitly low.
   */
  LOW_CONFIDENCE,

  /**
   * At least one profile value is represented as a range or statistical measurement instead of a point value.
   * This is informational: aggregated evidence can be more reliable than a single exact value.
   */
  NON_POINT_VALUES
}
