package org.craft.mcs.cap.core.measurement.domain.model;


/** Business reason for appending an immutable measurement-set revision. */
public enum MeasurementSetRevisionChangeKind {

  /** First recording of the logical measurement set. */
  INITIAL,

  /** Correction of metadata, evidence, or an individual characteristic result. */
  CORRECTION,

  /** Terminal withdrawal of the complete logical measurement set. */
  RETRACTION
}
