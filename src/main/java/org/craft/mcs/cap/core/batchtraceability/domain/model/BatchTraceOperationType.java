package org.craft.mcs.cap.core.batchtraceability.domain.model;


/**
 * Business operation recorded in material traceability.
 */
public enum BatchTraceOperationType {
  /**
   * Initial creation of a feedstock batch.
   */
  CREATE_FEEDSTOCK_BATCH,

  /**
   * Measurement added to a feedstock or production subject.
   */
  MEASUREMENT_ADDED,

  /**
   * Maceration or similar pre-production material treatment.
   */
  MACERATION,

  /**
   * Manual or calculated composition of several material sources.
   */
  COMPOSITION,

  /**
   * Split of one feedstock or production batch into several independent downstream batches.
   */
  SPLIT,

  /**
   * Explicit volume correction with a reason such as loss, sampling or discard.
   */
  VOLUME_ADJUSTMENT,

  /**
   * Accepted material input committed as a production batch.
   */
  CREATE_PRODUCTION_BATCH,

  /**
   * Production operation that starts fermentation.
   */
  START_FERMENTATION,

  /**
   * Yeast pitch operation.
   */
  YEAST_PITCHING,

  /**
   * Racking or transfer from sediment.
   */
  RACKING,

  /**
   * Bottling or packaging operation.
   */
  BOTTLING,

  /**
   * Final chemical or sensory assessment attached to a produced lot.
   */
  FINAL_ASSESSMENT
}
