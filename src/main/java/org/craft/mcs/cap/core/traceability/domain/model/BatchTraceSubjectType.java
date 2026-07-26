package org.craft.mcs.cap.core.traceability.domain.model;


/**
 * Type of material aggregate whose immutable states participate in batch traceability.
 *
 * <p>Measurements, calculations and assessments are evidence or operation facts. They do not
 * represent material and therefore are not trace subjects.
 */
public enum BatchTraceSubjectType {
  /**
   * Liquid source material managed as a FeedstockBatch.
   */
  FEEDSTOCK_BATCH,

  /**
   * Bulk material managed through the ProductionBatch lifecycle.
   */
  PRODUCTION_BATCH
}
