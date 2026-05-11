package org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain;


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
