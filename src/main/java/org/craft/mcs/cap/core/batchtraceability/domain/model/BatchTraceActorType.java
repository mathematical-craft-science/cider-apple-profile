package org.craft.mcs.cap.core.batchtraceability.domain.model;


/** Identifies the kind of actor responsible for an immutable traceability fact. */
public enum BatchTraceActorType {
  /** A human CAP account user. */
  ACCOUNT_USER,

  /** A future non-human integration identity. */
  MACHINE_IDENTITY,

  /** A named CAP system process. */
  SYSTEM
}
