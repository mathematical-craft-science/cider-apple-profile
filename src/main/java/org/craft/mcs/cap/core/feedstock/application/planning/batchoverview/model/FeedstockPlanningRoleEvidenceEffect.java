package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model;


/**
 * Typed effect of one source axis on a target-aware composition candidate.
 */
public enum FeedstockPlanningRoleEvidenceEffect {

  /**
   * Source can raise specific gravity in the selected composition context.
   */
  RAISES_SUGAR,

  /**
   * Source can raise titratable acidity in the selected composition context.
   */
  RAISES_ACIDITY,

  /**
   * Source can add tannin structure in the selected composition context.
   */
  BUILDS_TANNIN_STRUCTURE,

  /**
   * Source can soften an overly acidic blend.
   */
  SOFTENS_ACIDITY_HEAVY_BLEND,

  /**
   * Source can soften an overly tannic blend.
   */
  SOFTENS_TANNIN_HEAVY_BLEND,

  /**
   * Source mostly dilutes sugar and acidity for the selected context.
   */
  DILUTES_SUGAR_AND_ACIDITY,

  /**
   * No clear useful effect was derived from this evidence.
   */
  NO_CLEAR_EFFECT
}
