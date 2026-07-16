package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model;


/**
 * Strength of a typed role-evidence fact for planning interpretation.
 */
public enum FeedstockPlanningRoleEvidenceSeverity {

  /**
   * Evidence positively supports the assigned contribution role.
   */
  SUPPORTING,

  /**
   * Evidence is present but does not strongly support or limit the role.
   */
  NEUTRAL,

  /**
   * Evidence indicates a limitation or weak fit for the selected target.
   */
  LIMITATION
}
