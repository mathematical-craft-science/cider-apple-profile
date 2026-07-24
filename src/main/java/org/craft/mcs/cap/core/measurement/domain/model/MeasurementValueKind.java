package org.craft.mcs.cap.core.measurement.domain.model;


/** Shape retained by a measurement record. It does not describe sampling statistics. */
public enum MeasurementValueKind {

  /** One canonical point value, regardless of its provenance or confidence. */
  POINT,

  /** Canonical lower and upper bounds that do not imply repeated observations or a mean. */
  INTERVAL
}
