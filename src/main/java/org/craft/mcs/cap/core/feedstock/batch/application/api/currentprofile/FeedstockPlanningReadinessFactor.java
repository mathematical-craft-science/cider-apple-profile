package org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile;


/**
 * Input factor checked before a feedstock batch is used in production planning.
 *
 * <p>The factors describe source conditions of the batch itself. Derived analysis results, such as
 * sugar-acidity graph projection, should stay in dedicated analysis/data-quality read models rather
 * than become readiness factors.
 */
public enum FeedstockPlanningReadinessFactor {

  /**
   * Specific Gravity is available in the current material profile. It supports sugar-acidity
   * analysis, potential ABV prediction and composition planning.
   */
  SPECIFIC_GRAVITY,

  /**
   * Titratable Acidity is available in the current profile. It is required for sugar-acidity
   * analysis and composition planning.
   */
  TITRATABLE_ACIDITY,

  /**
   * Tannin is available in the current characteristic state. Missing tannin does not block basic
   * composition planning, but makes target-aware structure assessment incomplete.
   */
  TANNIN,

  /**
   * pH is assessed as planning guidance. It does not participate in target distance or composition
   * optimization, but helps the technologist notice yeast-stress or microbial-risk preparation needs.
   */
  PH,

  /**
   * Available feedstock volume is present. A batch with zero available volume cannot be selected
   * as a material source for a production candidate.
   */
  VOLUME
}
