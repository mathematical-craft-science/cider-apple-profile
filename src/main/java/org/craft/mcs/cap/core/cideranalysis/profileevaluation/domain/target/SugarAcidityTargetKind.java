package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target;


public enum SugarAcidityTargetKind {

  /**
   * Target selected from built-in sugar-acidity graph zones.
   */
  PREDEFINED_ZONE,

  /**
   * Target explicitly defined by a technologist or client request.
   */
  CUSTOM_RECTANGLE
}
