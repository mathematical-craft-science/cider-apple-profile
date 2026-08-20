package org.craft.mcs.cap.core.cideranalysis.abv.domain;


/**
 * Algorithm family used for ABV prediction.
 */
public enum AbvPredictionMethod {
  /**
   * Standard cider/brewing approximation based on original gravity minus final gravity.
   */
  STANDARD_GRAVITY_DROP
}
