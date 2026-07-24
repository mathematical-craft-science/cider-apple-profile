package org.craft.mcs.cap.core.measurement.domain.model;


/** Lifecycle of a logical measurement set; retracted evidence remains physically preserved. */
public enum MeasurementSetLifecycle {

  /** The latest revision may contribute working RESULT records to the current profile. */
  ACTIVE,

  /** The set is excluded from current projections while every revision remains preserved. */
  RETRACTED
}
