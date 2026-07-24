package org.craft.mcs.cap.core.measurement.domain.model;


/** Origin category of measurement evidence. */
public enum MeasurementSourceType {

  /** A person entered the value directly without a registered instrument or external authority. */
  USER,

  /** A canonical value was obtained from or attributed to a measurement instrument. */
  INSTRUMENT,

  /** A laboratory supplied the value. */
  LAB,

  /** An external dataset or document import supplied the value. */
  IMPORT,

  /** A catalog curator supplied or approved the value. */
  CURATOR,

  /** A research or educational institute supplied the value. */
  INSTITUTE,

  /** CAP transparently derived the working result from immutable observation records. */
  DERIVED
}
