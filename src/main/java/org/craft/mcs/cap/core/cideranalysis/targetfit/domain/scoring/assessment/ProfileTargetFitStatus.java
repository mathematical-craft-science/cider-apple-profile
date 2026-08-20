package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.assessment;

/** Stable target-fit interpretation intended for application and API consumers. */
public enum ProfileTargetFitStatus {

  /** Profile has at least a 90 percent target match. */
  EXCELLENT,

  /** Profile has at least a 70 percent target match but is below the excellent threshold. */
  CLOSE,

  /** Profile was scored but requires adjustment to approach the target. */
  NEEDS_ADJUSTMENT,

  /** Target fit cannot be assessed because measurements required by the objective are missing. */
  UNSCORABLE
}
