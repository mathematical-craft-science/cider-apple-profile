package org.craft.mcs.cap.core.production.candidate.domain.model;

/** Lifecycle state of a saved production candidate. */
public enum ProductionBatchCandidateStatus {

  /** Candidate may still be confirmed as a production batch. */
  AVAILABLE,

  /** Candidate has already created a production batch and cannot be reused. */
  CONFIRMED
}
