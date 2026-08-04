package org.craft.mcs.cap.core.measurement.domain.model;


/** Semantic role of one immutable measurement record within a revision. */
public enum MeasurementRecordRole {
  /** One independently observed value retained as metrological evidence. */
  OBSERVATION,
  /** The single value used by current-profile projection for this characteristic. */
  RESULT
}
