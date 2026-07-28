package org.craft.mcs.cap.core.profileanalysis.profileaxis.domain;


/**
 * Analysis scenarios supported by the available canonical profile characteristics.
 */
public enum ChemicalProfileCapability {

  /**
   * Specific Gravity and Titratable Acidity are available for two-dimensional graph projection.
   */
  SUGAR_ACIDITY_GRAPH,

  /**
   * Specific Gravity, Titratable Acidity and tannin are available for North American classification.
   */
  NORTH_AMERICAN_PROFILE,

  /**
   * Specific Gravity is available for potential ABV prediction.
   */
  ABV_PREDICTION
}
