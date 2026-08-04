package org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain;


/** Ways a technologist can define an SG/TA target. */
public enum SugarAcidityTargetKind {

  /**
   * Target selected from built-in SG/TA interpretation zones.
   */
  PREDEFINED_ZONE,

  /**
   * Target explicitly defined by a technologist or client request.
   */
  CUSTOM_RECTANGLE
}
