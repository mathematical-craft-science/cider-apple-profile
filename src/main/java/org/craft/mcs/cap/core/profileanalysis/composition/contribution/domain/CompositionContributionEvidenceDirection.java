package org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain;


/** Direction of one source characteristic relative to its nearest target coordinate. */
public enum CompositionContributionEvidenceDirection {

  /** Source value is below the nearest target coordinate. */
  BELOW_TARGET,

  /** Source value equals the nearest target coordinate. */
  ON_TARGET,

  /** Source value is above the nearest target coordinate. */
  ABOVE_TARGET
}
