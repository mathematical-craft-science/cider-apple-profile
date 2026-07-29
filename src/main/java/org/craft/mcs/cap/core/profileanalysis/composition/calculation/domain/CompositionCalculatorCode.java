package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;


/**
 * Stable codes for selecting a composition calculator.
 *
 * <p>The code travels through application commands and REST DTOs. The actual implementation is
 * resolved by {@link CompositionCalculatorSelector}, so API callers do not depend on
 * concrete strategy class names.
 */
public enum CompositionCalculatorCode {

  /**
   * Calculates SG, TA, tannin, and other supported characteristics by mass balance.
   */
  MASS_BALANCE
}
