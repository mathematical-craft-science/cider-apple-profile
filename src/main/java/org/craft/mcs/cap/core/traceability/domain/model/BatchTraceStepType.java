package org.craft.mcs.cap.core.traceability.domain.model;


/**
 * Kind of an immutable fact currently recorded by batch traceability.
 *
 * <p>This enum deliberately does not model the future production-operation catalog. A new step type
 * is introduced only together with the use case and invariants that produce that trace fact.
 */
public enum BatchTraceStepType {
  /**
   * A feedstock batch and its initial material state were created.
   */
  FEEDSTOCK_BATCH_CREATED,

  /**
   * A production batch and its initial material state were created from accepted source states.
   */
  PRODUCTION_BATCH_CREATED
}
