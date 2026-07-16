package org.craft.mcs.cap.core.feedstock.application.currentprofile.model;

/** Stable machine-readable outcomes of feedstock planning-readiness checks. */
public enum FeedstockPlanningReadinessCheckCode {

  /** No current measurement exists for the batch. */
  CURRENT_MEASUREMENT_MISSING,

  /** A current measurement exists for the batch. */
  CURRENT_MEASUREMENT_PRESENT,

  /** Available volume is zero and cannot be allocated to a production candidate. */
  AVAILABLE_VOLUME_EMPTY,

  /** Positive available volume can be allocated to a production candidate. */
  AVAILABLE_VOLUME_PRESENT,

  /** Sugar is available for planning and target analysis. */
  SUGAR_PRESENT,

  /** Sugar required for planning and target analysis is missing. */
  SUGAR_MISSING,

  /** Titratable acidity is available for planning and target analysis. */
  TITRATABLE_ACIDITY_PRESENT,

  /** Titratable acidity required for planning and target analysis is missing. */
  TITRATABLE_ACIDITY_MISSING,

  /** Tannin is available for target-aware structure assessment. */
  TANNIN_PRESENT,

  /** Tannin is missing and structure assessment is incomplete. */
  TANNIN_MISSING,

  /** pH is within the curated planning guidance range. */
  PH_PLANNING_IDEAL,

  /** pH is below the curated planning guidance range. */
  PH_PLANNING_LOW,

  /** pH is above the curated planning guidance range. */
  PH_PLANNING_HIGH,

  /** pH is missing and its planning risks cannot be assessed. */
  PH_MISSING
}
