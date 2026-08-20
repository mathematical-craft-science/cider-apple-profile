package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.contribution;


/** Cider-making interpretation of one target-aware contribution evidence fact. */
public enum CompositionContributionEvidenceEffect {

  /** Higher SG indicates that the source can contribute more fermentable sugar potential. */
  RAISES_SUGAR_POTENTIAL,

  /** Lower SG indicates that the source can reduce sugar potential in a composition. */
  LOWERS_SUGAR_POTENTIAL,

  /** Higher TA indicates that the source can contribute more acidity. */
  RAISES_ACIDITY,

  /** Lower TA indicates that the source can soften an acidity-heavy composition. */
  SOFTENS_ACIDITY_HEAVY_BLEND,

  /** Higher tannin indicates that the source can build tannin structure. */
  BUILDS_TANNIN_STRUCTURE,

  /** Lower tannin indicates that the source can soften a tannin-heavy composition. */
  SOFTENS_TANNIN_HEAVY_BLEND,

  /** Source and target coordinates coincide, so no directional effect is inferred. */
  NO_DIRECTIONAL_EFFECT
}
