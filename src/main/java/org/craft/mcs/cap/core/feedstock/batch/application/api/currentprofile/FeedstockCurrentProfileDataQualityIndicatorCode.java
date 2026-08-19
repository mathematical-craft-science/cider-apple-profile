package org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile;


/**
 * Stable machine-readable data-quality and evidence-shape indicator for the current feedstock profile.
 */
public enum FeedstockCurrentProfileDataQualityIndicatorCode {

  /**
   * The batch has no current profile evidence, so planning cannot use a reliable current chemical state.
   */
  CURRENT_PROFILE_MISSING,

  /**
   * The current profile has enough SG and TA evidence for the sugar-acidity graph.
   */
  SUGAR_ACIDITY_GRAPH_PLOTTABLE,

  /**
   * Specific Gravity is missing, so sugar-acidity projection and composition planning are blocked.
   */
  SPECIFIC_GRAVITY_MISSING,

  /**
   * Titratable Acidity is missing, so sugar-acidity projection and composition planning are blocked.
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
   * At least one required current-profile evidence item is old enough that the profile may no longer
   * describe the current feedstock state.
   */
  MEASUREMENT_STALE,

  /**
   * Measurement confidence is missing, so the user cannot judge trust level explicitly.
   */
  CONFIDENCE_MISSING,

  /**
   * Measurement confidence is explicitly low.
   */
  LOW_CONFIDENCE,

  /**
   * At least one profile value is represented as a source-reported interval instead of a point value.
   * This is informational: an interval expresses bounded uncertainty and is not a statistical sample.
   */
  NON_POINT_VALUES
}
