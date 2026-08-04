package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;


/**
 * Source categories accepted for inbound measurement evidence.
 *
 * <p>Derived results are deliberately absent: CAP creates them from repeated observations and
 * clients cannot submit them as original evidence.
 */
public enum MeasurementInputSourceType {

  /** A person entered the value directly. */
  USER,

  /** A measurement instrument supplied the value. */
  INSTRUMENT,

  /** A laboratory supplied the value. */
  LAB,

  /** An external dataset or document import supplied the value. */
  IMPORT,

  /** A catalog curator supplied or approved the value. */
  CURATOR,

  /** A research or educational institute supplied the value. */
  INSTITUTE
}
