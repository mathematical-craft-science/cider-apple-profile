package org.craft.mcs.cap.core.profileanalysis.abv.domain;


public enum PredictionConfidence {
  /**
   * Baseline estimate based on defaults or sparse data, for example fixed dry-cider FG.
   */
  LOW,

  /**
   * Estimate adjusted by meaningful domain context, for example yeast characteristics.
   */
  MEDIUM,

  /**
   * Estimate supported by concrete fermentation batch measurements over time.
   */
  HIGH
}
