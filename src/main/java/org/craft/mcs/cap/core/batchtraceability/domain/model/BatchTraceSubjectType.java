package org.craft.mcs.cap.core.batchtraceability.domain.model;


/**
 * Type of business object participating in batch traceability.
 */
public enum BatchTraceSubjectType {
  /**
   * Feedstock batch material before production.
   */
  FEEDSTOCK_BATCH,

  /**
   * Measurement recorded for feedstock material.
   */
  FEEDSTOCK_MEASUREMENT,

  /**
   * Accepted or calculated composition result used as a production input.
   */
  COMPOSITION_RESULT,

  /**
   * Production batch created from accepted material input.
   */
  PRODUCTION_BATCH,

  /**
   * Fermentation process reading.
   */
  FERMENTATION_READING,

  /**
   * Bottled or packaged lot.
   */
  BOTTLE_LOT,

  /**
   * Final product chemical or sensory assessment.
   */
  FINISHED_PRODUCT_ASSESSMENT
}
