package org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in;


/**
 * Input factor checked before a feedstock batch is used in production planning.
 *
 * <p>The factors describe source conditions of the batch itself. Derived analysis results, such as
 * sugar-acidity graph projection, should stay in dedicated analysis/data-quality read models rather
 * than become readiness factors.
 */
public enum FeedstockPlanningReadinessFactor {

  /**
   * A latest/current measurement is available for the batch. Without it, planning cannot reason
   * about the current chemical state of the feedstock.
   */
  CURRENT_MEASUREMENT,

  /**
   * Sugar concentration is available in the current chemical profile. This is a required input for
   * sugar-acidity analysis, ABV prediction, and composition planning.
   */
  SUGAR,

  /**
   * Titratable acidity is available in the current chemical profile. This is a required input for
   * sugar-acidity analysis and composition planning.
   */
  TITRATABLE_ACIDITY,

  /**
   * Tannin is available in the current chemical profile. Missing tannin does not block basic
   * composition planning, but makes target-aware structure assessment incomplete.
   */
  TANNIN,

  /**
   * Available feedstock volume is present. A batch with zero available volume cannot be selected
   * as a material source for a production candidate.
   */
  VOLUME
}
