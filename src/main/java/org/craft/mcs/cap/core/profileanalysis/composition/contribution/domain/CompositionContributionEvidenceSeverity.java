package org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain;


/** Strength of one typed contribution evidence fact within the complete role assessment. */
public enum CompositionContributionEvidenceSeverity {

  /** Evidence supports one or more assigned contribution roles. */
  SUPPORTING,

  /** Evidence has no directional contribution because source and target coordinates coincide. */
  NEUTRAL
}
