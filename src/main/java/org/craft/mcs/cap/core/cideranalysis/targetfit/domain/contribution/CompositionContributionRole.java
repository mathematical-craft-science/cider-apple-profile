package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.contribution;


/**
 * Cider-making interpretation of how one source can contribute to a selected composition target.
 *
 * <p>Sugar and acidity roles are inferred from canonical Specific Gravity and Titratable Acidity
 * evidence. They describe planning utility and do not claim direct sensory sweetness or perceived
 * acidity.
 */
public enum CompositionContributionRole {

  /**
   * Source is already inside the selected SG/TA target area and can act as a main base.
   */
  BALANCED_BASE,

  /**
   * Source has higher SG than the nearest target coordinate and can contribute more fermentable
   * sugar potential.
   */
  SUGAR_CONTRIBUTOR,

  /**
   * Source has lower SG than the nearest target coordinate and can reduce fermentable sugar
   * potential in a composition.
   */
  SUGAR_REDUCER,

  /**
   * Source has higher TA than the nearest target coordinate and can contribute more acidity.
   */
  ACIDITY_CONTRIBUTOR,

  /**
   * Source is above the target tannin coordinate and can add structure/astringency.
   */
  TANNIN_CONTRIBUTOR,

  /**
   * Source has lower TA than the nearest target coordinate and can reduce the composition's
   * acidity indicator.
   */
  ACIDITY_REDUCER,

  /**
   * Source is below the target tannin coordinate and can soften an overly tannic composition.
   */
  TANNIN_SOFTENER,

  /**
   * Source is below both target SG and TA coordinates and mostly dilutes sugar and acidity
   * indicators.
   */
  DILUTION_SOURCE
}
