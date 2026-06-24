package org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain;


/**
 * Semantic role of one chemical profile source relative to a selected production composition
 * target.
 */
public enum CompositionContributionRole {

  /**
   * Source is already inside the selected target profile and can act as a main base.
   */
  BALANCED_BASE,

  /**
   * Source is above the target sugar coordinate and can raise specific gravity.
   */
  SUGAR_CONTRIBUTOR,

  /**
   * Source is above the target acidity coordinate and can raise titratable acidity.
   */
  ACIDITY_CONTRIBUTOR,

  /**
   * Source is above the target tannin coordinate and can add structure/astringency.
   */
  TANNIN_CONTRIBUTOR,

  /**
   * Source is below the target acidity coordinate and can soften an overly acidic composition.
   */
  ACIDITY_REDUCER,

  /**
   * Source is below the target tannin coordinate and can soften an overly tannic composition.
   */
  TANNIN_SOFTENER,

  /**
   * Source is below both target sugar and acidity coordinates and mostly acts as a diluting source.
   */
  DILUTION_SOURCE,

  /**
   * Source is assessable but does not provide a clear useful contribution to the selected target.
   */
  LOW_FIT_SOURCE,

  /**
   * Source is missing required axes for target-aware role assessment.
   */
  UNASSESSABLE
}
