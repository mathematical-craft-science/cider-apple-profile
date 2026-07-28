package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain;


/**
 * Canonical dimensions admitted into profile-target distance and contribution-role analysis.
 */
public enum ProfileEvaluationAxis {

  /**
   * Specific gravity coordinate used by the profile evaluation objective space.
   */
  SPECIFIC_GRAVITY,

  /**
   * Titratable acidity coordinate used by the profile evaluation objective space.
   */
  TITRATABLE_ACIDITY,

  /**
   * Optional tannin coordinate used by extended profile target scoring.
   */
  TANNIN
}
