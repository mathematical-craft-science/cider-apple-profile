package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;


/**
 * Stable codes for selecting a chemical profile composition calculator.
 *
 * <p>The code travels through application commands and REST DTOs. The actual implementation is
 * resolved by {@link ChemicalProfileCompositionCalculatorSelector}, so API callers do not depend on
 * concrete strategy class names.
 */
public enum ChemicalProfileCompositionCalculatorCode {

  /**
   * Calculates SG, TA, tannin, and other supported characteristics by mass balance.
   */
  MASS_BALANCE
}
